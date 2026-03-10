package config

import (
	"fmt"
	"time"

	"github.com/spf13/viper"
)

type Config struct {
	Backend    BackendConfig    `mapstructure:"backend"`
	RabbitMQ   RabbitMQConfig   `mapstructure:"rabbitmq"`
	Simulation SimulationConfig `mapstructure:"simulation"`
	Warehouse  WarehouseConfig  `mapstructure:"warehouse"`
}

type BackendConfig struct {
	APIURL         string `mapstructure:"api_url"`
	LoadWarehouses bool   `mapstructure:"load_warehouses"`
	AuthToken      string `mapstructure:"auth_token"`
}
type RabbitMQConfig struct {
	Host             string `mapstructure:"host"`
	Port             int    `mapstructure:"port"`
	Username         string `mapstructure:"username"`
	Password         string `mapstructure:"password"`
	Exchange         string `mapstructure:"exchange"`
	TemperatureQueue string `mapstructure:"temperature_queue"`
	HeartbeatQueue   string `mapstructure:"heartbeat_queue"`
}

func (r *RabbitMQConfig) GetConnectionString() string {
	return fmt.Sprintf("amqp://%s:%s@%s:%d/", r.Username, r.Password, r.Host, r.Port)
}

type SimulationConfig struct {
	HeartbeatInterval   time.Duration `mapstructure:"heartbeat_interval"`
	TemperatureInterval time.Duration `mapstructure:"temperature_interval"`
}

type WarehouseConfig struct {
	ID      int64          `mapstructure:"id"`
	Name    string         `mapstructure:"name"`
	Sectors []SectorConfig `mapstructure:"sectors"`
}

type SectorConfig struct {
	SectorID    int64   `mapstructure:"sector_id"`
	BaseTemp    float64 `mapstructure:"base_temp"`
	MaxVariance float64 `mapstructure:"max_variance"`
}

func LoadConfig(filename string) (*Config, error) {
	viper.SetConfigFile(filename)
	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		return nil, err
	}

	var config Config
	if err := viper.Unmarshal(&config); err != nil {
		return nil, err
	}

	return &config, nil
}
