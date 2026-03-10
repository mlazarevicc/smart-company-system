package config

import (
	"fmt"
	"os"
	"time"

	"gopkg.in/yaml.v3"
)

type Config struct {
	RabbitMQ   RabbitMQConfig   `yaml:"rabbitmq"`
	Backend    BackendConfig    `yaml:"backend"`
	Simulation SimulationConfig `yaml:"simulation"`
	Vehicles   []VehicleConfig  `yaml:"static_vehicles"`
}

type BackendConfig struct {
	APIURL       string `yaml:"api_url"`
	LoadVehicles bool   `yaml:"load_vehicles"`
	AuthToken    string `yaml:"auth_token"`
}

type RabbitMQConfig struct {
	Host           string `yaml:"host"`
	Port           int    `yaml:"port"`
	Username       string `yaml:"username"`
	Password       string `yaml:"password"`
	Exchange       string `yaml:"exchange"`
	DistanceQueue  string `yaml:"distance_queue"`
	HeartbeatQueue string `yaml:"heartbeat_queue"`
}

type SimulationConfig struct {
	HeartbeatInterval time.Duration `yaml:"heartbeat_interval"`
	DistanceInterval  time.Duration `yaml:"distance_interval"`
}

type VehicleConfig struct {
	ID                 int64   `yaml:"id"`
	RegistrationNumber string  `yaml:"registration_number"`
	Latitude           float64 `yaml:"latitude"`
	Longitude          float64 `yaml:"longitude"`
	Distance           float64 `yaml:"distance"`
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
	if c.Backend.LoadVehicles && c.Backend.APIURL == "" {
		return fmt.Errorf("backend api_url is required when load_vehicles is true")
	}
	return nil
}

func (r *RabbitMQConfig) GetConnectionString() string {
	return fmt.Sprintf("amqp://%s:%s@%s:%d/", r.Username, r.Password, r.Host, r.Port)
}
