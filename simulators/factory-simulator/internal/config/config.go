package config

import (
	"fmt"
	"os"
	"time"

	"gopkg.in/yaml.v3"
)

type Config struct {
	RabbitMQ   RabbitMQConfig   `yaml:"rabbitmq"`
	Factory    FactoryConfig    `yaml:"factory"`
	Simulation SimulationConfig `yaml:"simulation"`
}

type RabbitMQConfig struct {
	Host            string `yaml:"host"`
	Port            int    `yaml:"port"`
	Username        string `yaml:"username"`
	Password        string `yaml:"password"`
	Exchange        string `yaml:"exchange"`
	ProductionQueue string `yaml:"production_queue"`
	HeartbeatQueue  string `yaml:"heartbeat_queue"`
}

type FactoryConfig struct {
	ID       int64           `yaml:"id"`
	Name     string          `yaml:"name"`
	Products []ProductConfig `yaml:"products"`
}

type ProductConfig struct {
	ProductID   int64 `yaml:"product_id"`
	MinQuantity int64 `yaml:"min_quantity"`
	MaxQuantity int64 `yaml:"max_quantity"`
}

type SimulationConfig struct {
	HeartbeatInterval time.Duration `yaml:"heartbeat_interval"`
	ProductionTimes   []string      `yaml:"production_times"`
}

func LoadConfig(path string) (*Config, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("failed to read config file: %w", err)
	}

	var config Config
	if err := yaml.Unmarshal(data, &config); err != nil {
		return nil, fmt.Errorf("failed to parse config file: %w", err)
	}

	if err := config.Validate(); err != nil {
		return nil, fmt.Errorf("invalid configuration: %w", err)
	}

	return &config, nil
}

func (c *Config) Validate() error {
	if c.RabbitMQ.Host == "" {
		return fmt.Errorf("rabbitmq host is required")
	}
	if c.RabbitMQ.Exchange == "" {
		return fmt.Errorf("rabbitmq exchange is required")
	}
	if c.Factory.ID == 0 {
		return fmt.Errorf("factory ID is required")
	}
	if len(c.Factory.Products) == 0 {
		return fmt.Errorf("at least one product must be configured")
	}

	return nil
}

func (r *RabbitMQConfig) GetConnectionString() string {
	return fmt.Sprintf("amqp://%s:%s@%s:%d/",
		r.Username, r.Password, r.Host, r.Port)
}
