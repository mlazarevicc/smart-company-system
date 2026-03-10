package simulator

import (
	"log"
	"math/rand"
	"time"

	"factory-simulator/internal/config"
	"factory-simulator/internal/models"
	"factory-simulator/internal/rabbitmq"
)

type FactorySimulator struct {
	config    config.FactoryConfig
	simConfig config.SimulationConfig
	publisher *rabbitmq.Publisher
	stopChan  chan struct{}
}

func NewFactorySimulator(
	factoryConfig config.FactoryConfig,
	simConfig config.SimulationConfig,
	publisher *rabbitmq.Publisher,
) *FactorySimulator {
	return &FactorySimulator{
		config:    factoryConfig,
		simConfig: simConfig,
		publisher: publisher,
		stopChan:  make(chan struct{}),
	}
}

func (fs *FactorySimulator) Start() {
	log.Printf("🏭 Starting factory simulator: %s (ID: %d)", fs.config.Name, fs.config.ID)
	go fs.heartbeatLoop()
	go fs.productionLoop()
}

func (fs *FactorySimulator) Stop() {
	close(fs.stopChan)
	log.Printf("🛑 Stopped factory simulator: %s (ID: %d)", fs.config.Name, fs.config.ID)
}

func (fs *FactorySimulator) heartbeatLoop() {
	ticker := time.NewTicker(fs.simConfig.HeartbeatInterval)
	defer ticker.Stop()

	// Pošalji prvi heartbeat odmah pri pokretanju
	fs.sendHeartbeat()

	for {
		select {
		case <-ticker.C:
			fs.sendHeartbeat()
		case <-fs.stopChan:
			return
		}
	}
}

func (fs *FactorySimulator) productionLoop() {
	// Proveravamo vreme svake sekunde
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	var lastProducedTime string

	for {
		select {
		case <-ticker.C:
			now := time.Now().Format("15:04") // Format HH:MM

			// Proveri da li je vreme za proizvodnju i da već nismo proizveli u ovom minutu
			if now != lastProducedTime {
				for _, scheduledTime := range fs.simConfig.ProductionTimes {
					if now == scheduledTime {
						fs.produceItems()
						lastProducedTime = now
						break
					}
				}
			}
		case <-fs.stopChan:
			return
		}
	}
}

func (fs *FactorySimulator) sendHeartbeat() {
	message := models.HeartbeatMessage{
		FactoryID: fs.config.ID,
		Timestamp: time.Now(),
		Status:    "online", // Uvek je online dok aplikacija radi
	}

	err := fs.publisher.PublishHeartbeat(fs.config.ID, message)
	if err != nil {
		log.Printf("❌ [Factory %d] Failed to send heartbeat: %v", fs.config.ID, err)
	} else {
		log.Printf("💓 [Factory %d] Heartbeat sent", fs.config.ID)
	}
}

func (fs *FactorySimulator) produceItems() {
	productions := make([]models.ProductionDetail, 0, len(fs.config.Products))

	for _, productConfig := range fs.config.Products {
		quantity := productConfig.MinQuantity +
			rand.Int63n(productConfig.MaxQuantity-productConfig.MinQuantity+1)

		productions = append(productions, models.ProductionDetail{
			ProductID: productConfig.ProductID,
			Quantity:  quantity,
		})
	}

	message := models.ProductionMessage{
		FactoryID:   fs.config.ID,
		Timestamp:   time.Now(),
		Productions: productions,
	}

	err := fs.publisher.PublishProduction(fs.config.ID, message)
	if err != nil {
		log.Printf("❌ [Factory %d] Failed to send production: %v", fs.config.ID, err)
	} else {
		log.Printf("📦 [Factory %d] Production sent - %d products", fs.config.ID, len(productions))
		for _, prod := range productions {
			log.Printf("   └─ Product %d: %d units", prod.ProductID, prod.Quantity)
		}
	}
}
