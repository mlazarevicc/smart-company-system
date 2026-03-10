package main

import (
	"context"
	"log"
	"os"
	"os/signal"
	"syscall"

	"vehicle-simulator/internal/backend"
	"vehicle-simulator/internal/config"
	"vehicle-simulator/internal/rabbitmq"
	"vehicle-simulator/internal/simulator"
)

func main() {
	cfg, err := config.LoadConfig("config.yaml")
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	pub, err := rabbitmq.NewPublisher(
		cfg.RabbitMQ.GetConnectionString(),
		cfg.RabbitMQ.Exchange,
		cfg.RabbitMQ.DistanceQueue,
		cfg.RabbitMQ.HeartbeatQueue,
	)
	if err != nil {
		log.Fatalf("Failed to initialize RabbitMQ publisher: %v", err)
	}
	defer func() {
		log.Println("Closing RabbitMQ connection...")
		pub.Close()
	}()

	apiClient := backend.NewAPIClient(cfg.Backend)

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		<-sigChan
		log.Println("Received termination signal. Shutting down gracefully...")
		cancel()
	}()

	manager := simulator.NewManager(cfg, pub, apiClient)

	log.Println("Warehouse Simulator starting...")
	if err := manager.Start(ctx); err != nil {
		log.Fatalf("Simulator manager failed: %v", err)
	}

	log.Println("Simulator stopped cleanly.")
}
