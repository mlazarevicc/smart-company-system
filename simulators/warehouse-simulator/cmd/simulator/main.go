package main

import (
	"context"
	"flag"
	"log"
	"os"
	"os/signal"
	"syscall"

	"warehouse-simulator/internal/config"
	"warehouse-simulator/internal/rabbitmq"
	"warehouse-simulator/internal/simulator"
)

func main() {
	configPath := flag.String("config", "config.yaml", "Path to configuration file")
	flag.Parse()

	cfg, err := config.LoadConfig(*configPath)
	if err != nil {
		log.Fatalf("Failed to load config from %s: %v", *configPath, err)
	}

	pub, err := rabbitmq.NewPublisher(
		cfg.RabbitMQ.GetConnectionString(),
		cfg.RabbitMQ.Exchange,
		cfg.RabbitMQ.TemperatureQueue,
		cfg.RabbitMQ.HeartbeatQueue,
	)
	if err != nil {
		log.Fatalf("Failed to initialize RabbitMQ publisher: %v", err)
	}
	defer func() {
		log.Println("Closing RabbitMQ connection...")
		pub.Close()
	}()

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		<-sigChan
		log.Println("Received termination signal. Shutting down gracefully...")
		cancel()
	}()

	whSimulator := simulator.NewWarehouseSimulator(cfg, pub)

	if err := whSimulator.Start(ctx); err != nil {
		log.Fatalf("Simulator failed: %v", err)
	}

	log.Println("Simulator stopped cleanly.")
}
