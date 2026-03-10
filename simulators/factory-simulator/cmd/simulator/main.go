package main

import (
	"flag"
	"log"
	"os"
	"os/signal"
	"syscall"

	"factory-simulator/internal/config"
	"factory-simulator/internal/rabbitmq"
	"factory-simulator/internal/simulator"
)

func main() {
	log.Println("Factory Simulator Starting...")

	// Parse command line flags
	configPath := flag.String("config", "config.yaml", "Path to config file")
	flag.Parse()

	// Load configuration
	log.Printf("📄 Loading configuration from: %s", *configPath)
	cfg, err := config.LoadConfig(*configPath)
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}
	log.Println("✅ Configuration loaded successfully")

	// Connect to RabbitMQ
	publisher, err := rabbitmq.NewPublisher(
		cfg.RabbitMQ.GetConnectionString(),
		cfg.RabbitMQ.Exchange,
	)
	if err != nil {
		log.Fatalf("Failed to connect to RabbitMQ: %v", err)
	}
	defer publisher.Close()

	// Pokretanje pojedinačnog simulatora fabrike
	log.Printf("🚀 Starting Factory Simulator for: %s", cfg.Factory.Name)

	factorySim := simulator.NewFactorySimulator(cfg.Factory, cfg.Simulation, publisher)
	factorySim.Start()

	// Wait for interrupt signal
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)
	<-sigChan

	log.Println("🛑 Shutdown signal received, stopping simulator...")
	factorySim.Stop()
	log.Println("👋 Factory Simulator stopped")
}
