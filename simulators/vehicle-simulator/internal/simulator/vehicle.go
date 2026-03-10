package simulator

import (
	"context"
	"log"
	"math/rand"
	"time"

	"vehicle-simulator/internal/config"
	"vehicle-simulator/internal/models"
	"vehicle-simulator/internal/rabbitmq"
)

type VehicleSimulator struct {
	config    config.VehicleConfig
	publisher *rabbitmq.Publisher
	heartbeat time.Duration
	distInt   time.Duration

	latitude  float64
	longitude float64
	distance  float64
}

func NewVehicleSimulator(cfg config.VehicleConfig, pub *rabbitmq.Publisher, hbInt, distInt time.Duration) *VehicleSimulator {
	lat := cfg.Latitude
	long := cfg.Longitude
	dist := cfg.Distance

	return &VehicleSimulator{
		config:    cfg,
		publisher: pub,
		heartbeat: hbInt,
		distInt:   distInt,
		latitude:  lat,
		longitude: long,
		distance:  dist,
	}
}

func (w *VehicleSimulator) Run(ctx context.Context) {
	log.Printf("Starting simulator for Vehicle ID %d (%s)", w.config.ID, w.config.RegistrationNumber)

	heartbeatTicker := time.NewTicker(w.heartbeat)
	defer heartbeatTicker.Stop()

	tempTicker := time.NewTicker(w.distInt)
	defer tempTicker.Stop()

	w.sendHeartbeat()
	w.sendDistance()

	for {
		select {
		case <-ctx.Done():
			log.Printf("Stopping simulator for Vehicle ID %d", w.config.ID)
			return
		case <-heartbeatTicker.C:
			w.sendHeartbeat()
		case <-tempTicker.C:
			w.simulateAndSendDistance()
		}
	}
}

func (w *VehicleSimulator) sendHeartbeat() {
	msg := models.HeartbeatMessage{
		VehicleID: w.config.ID,
		Timestamp: time.Now().UTC(),
		Status:    "online",
	}
	if err := w.publisher.PublishHeartbeat(msg); err != nil {
		log.Printf("[Vehicle %d] Failed to publish heartbeat: %v", w.config.ID, err)
	} else {
		log.Printf("[Vehicle %d] Heartbeat sent", w.config.ID)
	}
}

func (w *VehicleSimulator) simulateAndSendDistance() {
	currentLat := w.latitude
	currentLong := w.longitude
	currentDist := w.distance

	changeLat := (rand.Float64() * 90.0) - 45
	changeLong := (rand.Float64() * 180.0) - 90
	changeDist := rand.Float64()*0.1 - 0.05

	newLat := currentLat + changeLat
	newLong := currentLong + changeLong
	newDist := currentDist + changeDist

	// Ensure distance does not go negative
	if newDist < 0 {
		newDist = 0 // Set the distance to 0 if it goes negative
	}

	msg := models.DistanceMessage{
		VehicleID: w.config.ID,
		Timestamp: time.Now().UTC(),
		Latitude:  newLat,
		Longitude: newLong,
		Distance:  newDist,
	}

	if err := w.publisher.PublishDistance(msg); err != nil {
		log.Printf("[Vehicle %d] Failed to publish distance: %v", w.config.ID, err)
	} else {
		log.Printf("[Vehicle %d] Sent distance: %f, %f, %f", w.config.ID, msg.Distance, msg.Latitude, msg.Longitude)
	}
}

func (w *VehicleSimulator) sendDistance() {
	w.simulateAndSendDistance()
}
