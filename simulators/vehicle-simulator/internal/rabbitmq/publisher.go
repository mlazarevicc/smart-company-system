package rabbitmq

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

type Publisher struct {
	conn           *amqp.Connection
	channel        *amqp.Channel
	exchange       string
	distanceQueue  string
	heartbeatQueue string
}

func NewPublisher(connString, exchange, distQueue, heartbeatQueue string) (*Publisher, error) {
	conn, err := amqp.Dial(connString)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to RabbitMQ: %w", err)
	}

	channel, err := conn.Channel()
	if err != nil {
		conn.Close()
		return nil, fmt.Errorf("failed to open channel: %w", err)
	}

	err = channel.ExchangeDeclare(
		exchange,
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
		return nil, fmt.Errorf("failed to declare exchange: %w", err)
	}

	log.Printf("Connected to RabbitMQ")

	return &Publisher{
		conn:           conn,
		channel:        channel,
		exchange:       exchange,
		distanceQueue:  distQueue,
		heartbeatQueue: heartbeatQueue,
	}, nil
}

func (p *Publisher) PublishDistance(message interface{}) error {
	return p.publish(p.distanceQueue, message)
}

func (p *Publisher) PublishHeartbeat(message interface{}) error {
	return p.publish(p.heartbeatQueue, message)
}

func (p *Publisher) publish(routingKey string, message interface{}) error {
	body, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("failed to marshal message: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	err = p.channel.PublishWithContext(
		ctx,
		"",
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
	if p.channel != nil {
		p.channel.Close()
	}
	if p.conn != nil {
		p.conn.Close()
	}
	return nil
}
