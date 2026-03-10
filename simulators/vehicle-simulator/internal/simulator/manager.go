package simulator

import (
	"context"
	"log"
	"math/rand"
	"sync"
	"time"

	"vehicle-simulator/internal/backend"
	"vehicle-simulator/internal/config"
	"vehicle-simulator/internal/rabbitmq"
)

type Manager struct {
	config    *config.Config
	publisher *rabbitmq.Publisher
	apiClient *backend.APIClient
}

func NewManager(cfg *config.Config, pub *rabbitmq.Publisher, client *backend.APIClient) *Manager {
	return &Manager{
		config:    cfg,
		publisher: pub,
		apiClient: client,
	}
}

func (m *Manager) Start(ctx context.Context) error {
	var vehiclesToSimulate []config.VehicleConfig

	if m.config.Backend.LoadVehicles {
		log.Println("Loading vehicles from backend API...")
		backendVehicles, err := m.apiClient.FetchVehicles()
		if err != nil {
			log.Printf("Failed to load from backend: %v", err)
			log.Println("Falling back to static vehicles from config.yaml")
			vehiclesToSimulate = m.config.Vehicles
		} else {
			log.Printf("Successfully loaded %d vehicles from backend", len(backendVehicles))
			for _, bw := range backendVehicles {
				vehiclesToSimulate = append(vehiclesToSimulate, config.VehicleConfig{
					ID:      			   bw.ID,
					RegistrationNumber:    bw.RegistrationNumber,
				})
			}
		}
	} else {
		log.Println("Loading static vehicles from config.yaml...")
		vehiclesToSimulate = m.config.Vehicles
	}

	if len(vehiclesToSimulate) == 0 {
		log.Println("No vehicles to simulate. Exiting.")
		return nil
	}

	var wg sync.WaitGroup

	for _, wCfg := range vehiclesToSimulate {
		wg.Add(1)

		sim := NewVehicleSimulator(
			wCfg,
			m.publisher,
			m.config.Simulation.HeartbeatInterval,
			m.config.Simulation.DistanceInterval,
		)

		go func(simulator *VehicleSimulator) {
			defer wg.Done()
			simulator.Run(ctx)
		}(sim)
	}

	log.Printf("Started %d vehicle simulators", len(vehiclesToSimulate))

	wg.Wait()
	return nil
}

// TODO: what
func generateRandomSectorProfile() (float64, float64) {
	rand.Seed(time.Now().UnixNano())
	profileType := rand.Intn(3)

	switch profileType {
	case 0:
		return -18.0, 2.0
	case 1:
		return 4.0, 1.5
	default:
		return 22.0, 3.0
	}
}
