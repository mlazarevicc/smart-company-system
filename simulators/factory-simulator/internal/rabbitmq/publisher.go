package rabbitmq

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"sync"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

type Publisher struct {
	connUrl     string
	exchange    string
	conn        *amqp.Connection
	channel     *amqp.Channel
	mu          sync.RWMutex
	isConnected bool
	closeChan   chan *amqp.Error
}

func NewPublisher(connString, exchange string) (*Publisher, error) {
	p := &Publisher{
		connUrl:  connString,
		exchange: exchange,
	}

	// Prvo inicijalno povezivanje
	err := p.connect()
	if err != nil {
		return nil, err
	}

	// Pokrećemo gorutinu koja sluša gubitak konekcije i radi reconnect
	go p.handleReconnect()

	return p, nil
}

func (p *Publisher) connect() error {
	p.mu.Lock()
	defer p.mu.Unlock()

	conn, err := amqp.Dial(p.connUrl)
	if err != nil {
		return fmt.Errorf("failed to connect to RabbitMQ: %w", err)
	}

	channel, err := conn.Channel()
	if err != nil {
		conn.Close()
		return fmt.Errorf("failed to open channel: %w", err)
	}

	err = channel.ExchangeDeclare(
		p.exchange,
		"topic",
		true,
		false,
		false,
		false,
		nil,
	)
	if err != nil {
		channel.Close()
		conn.Close()
		return fmt.Errorf("failed to declare exchange: %w", err)
	}

	p.conn = conn
	p.channel = channel
	p.isConnected = true
	p.closeChan = make(chan *amqp.Error)
	p.conn.NotifyClose(p.closeChan)

	log.Printf("✅ Connected to RabbitMQ exchange: %s", p.exchange)
	return nil
}

func (p *Publisher) handleReconnect() {
	for {
		// Čekamo da konekcija pukne (ako stigne nil, znači da je namerno ugašena)
		err := <-p.closeChan
		if err == nil {
			return
		}

		log.Printf("⚠️ RabbitMQ connection lost: %v. Attempting to reconnect...", err)

		p.mu.Lock()
		p.isConnected = false
		p.mu.Unlock()

		// Pokušavamo da se konektujemo svakih 5 sekundi
		for {
			time.Sleep(5 * time.Second)
			log.Println("🔄 Reconnecting to RabbitMQ...")

			if err := p.connect(); err == nil {
				log.Println("✅ Successfully reconnected to RabbitMQ")
				break
			} else {
				log.Printf("❌ Reconnect failed: %v", err)
			}
		}
	}
}

func (p *Publisher) PublishProduction(factoryID int64, message interface{}) error {
	return p.publish(fmt.Sprintf("factory.%d.production", factoryID), message)
}

func (p *Publisher) PublishHeartbeat(factoryID int64, message interface{}) error {
	return p.publish(fmt.Sprintf("factory.%d.heartbeat", factoryID), message)
}

func (p *Publisher) publish(routingKey string, message interface{}) error {
	p.mu.RLock()
	if !p.isConnected {
		p.mu.RUnlock()
		return fmt.Errorf("not connected to RabbitMQ, message dropped")
	}
	// Kopiramo reference dok smo pod read-lockom da bismo izbegli race condition
	channel := p.channel
	exchange := p.exchange
	p.mu.RUnlock()

	body, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("failed to marshal message: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	err = channel.PublishWithContext(
		ctx,
		exchange,
		routingKey,
		false,
		false,
		amqp.Publishing{
			ContentType:  "application/json",
			Body:         body,
			DeliveryMode: amqp.Persistent,
			Timestamp:    time.Now(),
		},
	)

	if err != nil {
		return fmt.Errorf("failed to publish message: %w", err)
	}

	return nil
}

func (p *Publisher) Close() error {
	p.mu.Lock()
	defer p.mu.Unlock()

	p.isConnected = false

	if p.channel != nil {
		p.channel.Close()
	}
	if p.conn != nil {
		return p.conn.Close()
	}
	return nil
}
