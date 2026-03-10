package rabbitmq

import (
	"encoding/json"
	"fmt"
	"warehouse-simulator/internal/models"

	amqp "github.com/rabbitmq/amqp091-go"
)

type Publisher struct {
	conn             *amqp.Connection
	ch               *amqp.Channel
	exchange         string
	temperatureQueue string
	heartbeatQueue   string
}

func NewPublisher(url, exchange, tempQueue, hbQueue string) (*Publisher, error) {
	conn, err := amqp.Dial(url)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to RabbitMQ: %w", err)
	}

	ch, err := conn.Channel()
	if err != nil {
		conn.Close()
		return nil, fmt.Errorf("failed to open a channel: %w", err)
	}

	err = ch.ExchangeDeclare(
		exchange,
		"topic",
		true,
		false,
		false,
		false,
		nil,
	)
	if err != nil {
		ch.Close()
		conn.Close()
		return nil, fmt.Errorf("failed to declare an exchange: %w", err)
	}

	return &Publisher{
		conn:             conn,
		ch:               ch,
		exchange:         exchange,
		temperatureQueue: tempQueue,
		heartbeatQueue:   hbQueue,
	}, nil
}

func (p *Publisher) Close() {
	if p.ch != nil {
		p.ch.Close()
	}
	if p.conn != nil {
		p.conn.Close()
	}
}

func (p *Publisher) publish(routingKey string, message interface{}) error {
	body, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("failed to marshal message: %w", err)
	}

	err = p.ch.Publish(
		p.exchange, // exchange
		routingKey, // routing key
		false,      // mandatory
		false,      // immediate
		amqp.Publishing{
			ContentType: "application/json",
			Body:        body,
		})

	if err != nil {
		return fmt.Errorf("failed to publish message to %s: %w", routingKey, err)
	}

	return nil
}

func (p *Publisher) PublishTemperature(msg models.TemperatureMessage) error {
	return p.publish(p.temperatureQueue, msg)
}

func (p *Publisher) PublishHeartbeat(msg models.HeartbeatMessage) error {
	return p.publish(p.heartbeatQueue, msg)
}
