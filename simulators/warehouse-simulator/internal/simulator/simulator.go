package simulator

import (
	"context"
	"log"
	"math/rand"
	"time"

	"warehouse-simulator/internal/config"
	"warehouse-simulator/internal/models"
	"warehouse-simulator/internal/rabbitmq"
)

type WarehouseSimulator struct {
	config    *config.Config
	publisher *rabbitmq.Publisher
}

func NewWarehouseSimulator(cfg *config.Config, pub *rabbitmq.Publisher) *WarehouseSimulator {
	return &WarehouseSimulator{
		config:    cfg,
		publisher: pub,
	}
}

func (ws *WarehouseSimulator) Start(ctx context.Context) error {
	log.Printf("Starting simulator for Warehouse ID: %d (%s)", ws.config.Warehouse.ID, ws.config.Warehouse.Name)

	heartbeatTicker := time.NewTicker(ws.config.Simulation.HeartbeatInterval)
	defer heartbeatTicker.Stop()

	temperatureTicker := time.NewTicker(ws.config.Simulation.TemperatureInterval)
	defer temperatureTicker.Stop()

	ws.sendHeartbeat()
	ws.sendTemperatures()

	for {
		select {
		case <-ctx.Done():
			log.Printf("Shutting down warehouse simulator %d", ws.config.Warehouse.ID)
			return nil
		case <-heartbeatTicker.C:
			ws.sendHeartbeat()
		case <-temperatureTicker.C:
			ws.sendTemperatures()
		}
	}
}

func (ws *WarehouseSimulator) sendHeartbeat() {
	msg := models.HeartbeatMessage{
		WarehouseID: ws.config.Warehouse.ID,
		Timestamp:   time.Now().Format("2006-01-02T15:04:05"),
		Status:      "ONLINE",
	}

	err := ws.publisher.PublishHeartbeat(msg)
	if err != nil {
		log.Printf("[Warehouse %d] Failed to send heartbeat: %v", ws.config.Warehouse.ID, err)
	} else {
		log.Printf("[Warehouse %d] Heartbeat sent.", ws.config.Warehouse.ID)
	}
}

func (ws *WarehouseSimulator) sendTemperatures() {
	var sectorTemps []models.SectorTemperature

	for _, sector := range ws.config.Warehouse.Sectors {
		variance := (rand.Float64() * sector.MaxVariance * 2) - sector.MaxVariance
		currentTemp := sector.BaseTemp + variance

		sectorTemps = append(sectorTemps, models.SectorTemperature{
			SectorID:    sector.SectorID,
			Temperature: currentTemp,
		})
	}

	msg := models.TemperatureMessage{
		WarehouseID:  ws.config.Warehouse.ID,
		Temperatures: sectorTemps,
		Timestamp:    time.Now().Format("2006-01-02T15:04:05"),
	}

	err := ws.publisher.PublishTemperature(msg)
	if err != nil {
		log.Printf("[Warehouse %d] Failed to send temperatures: %v", ws.config.Warehouse.ID, err)
	} else {
		log.Printf("[Warehouse %d] Temperatures sent for %d sectors.", ws.config.Warehouse.ID, len(sectorTemps))
	}
}
