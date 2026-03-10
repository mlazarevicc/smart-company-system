package main

import (
	"context"
	"fmt"
	"log"
	"math/rand"
	"time"

	"warehouse-simulator/internal/backend"
	"warehouse-simulator/internal/config"

	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	"github.com/influxdata/influxdb-client-go/v2/api/write"
)

const (
	InfluxURL    = "http://localhost:8086"
	InfluxToken  = "rnyi-_h_ibyWJ5Fw6drxzofQnNgWEBwWLhUib7M2oB6RceqNKbqLUwMTI9ccdMB_uOJahoaIAGgpJenVL47iPw==" // change this
	InfluxOrg    = "nvt_org"
	InfluxBucket = "nvt_bucket"
)

func main() {
	cfg, err := config.LoadConfig("config.yaml")
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	apiClient := backend.NewAPIClient(cfg.Backend)

	log.Println("Fetching warehouses from backend API...")
	backendWarehouses, err := apiClient.FetchWarehouses()
	if err != nil {
		log.Fatalf("Failed to fetch warehouses from backend. Is Spring Boot running? Error: %v", err)
	}

	if len(backendWarehouses) == 0 {
		log.Println("No warehouses returned from backend API. Please create some in your database first.")
		return
	}

	limit := 115
	if len(backendWarehouses) > limit {
		backendWarehouses = backendWarehouses[:limit]
	}

	client := influxdb2.NewClient(InfluxURL, InfluxToken)
	defer client.Close()

	writeAPI := client.WriteAPIBlocking(InfluxOrg, InfluxBucket)

	daysBack := 730
	tempInterval := 10 * time.Minute
	availabilityInterval := 30 * time.Minute

	endTime := time.Now()
	startTime := endTime.AddDate(0, 0, -daysBack)

	log.Printf("Seeding data from %s to %s for %d warehouses...", startTime.Format(time.RFC3339), endTime.Format(time.RFC3339), len(backendWarehouses))

	totalTempPoints := 0
	totalAvailabilityPoints := 0

	rand.Seed(time.Now().UnixNano())

	for _, w := range backendWarehouses {
		log.Printf("Generating data for Warehouse: %s (ID: %d)", w.Name, w.ID)

		var availPoints []*write.Point
		isOnline := true
		outageUntil := time.Time{}

		for t := startTime; t.Before(endTime); t = t.Add(availabilityInterval) {

			if isOnline {
				if rand.Float64() < 0.01 {
					isOnline = false
					downtimeDuration := time.Duration(rand.Intn(10)+2) * time.Hour
					outageUntil = t.Add(downtimeDuration)
				}
			} else {
				if t.After(outageUntil) {
					isOnline = true
				}
			}

			ap := influxdb2.NewPoint(
				"warehouse_availability",
				map[string]string{
					"warehouse_id": fmt.Sprintf("%d", w.ID),
				},
				map[string]interface{}{
					"online": isOnline,
					"status": "historical_seed",
				},
				t,
			)
			availPoints = append(availPoints, ap)

			if len(availPoints) >= 10000 {
				if err := writeAPI.WritePoint(context.Background(), availPoints...); err != nil {
					log.Printf("Error writing availability batch: %v", err)
				} else {
					totalAvailabilityPoints += len(availPoints)
				}
				availPoints = nil
			}
		}

		if len(availPoints) > 0 {
			if err := writeAPI.WritePoint(context.Background(), availPoints...); err != nil {
				log.Printf("Error writing remaining availability points: %v", err)
			} else {
				totalAvailabilityPoints += len(availPoints)
			}
		}

		for _, s := range w.Sectors {
			baseTemp, maxVar := generateRandomSectorProfile()
			log.Printf("  -> Sector: %d (Base Temp: %.1f, Variance: %.1f)", s.ID, baseTemp, maxVar)

			currentTemp := baseTemp
			var tempPoints []*write.Point

			for t := startTime; t.Before(endTime); t = t.Add(tempInterval) {
				change := (rand.Float64() * 1.0) - 0.5
				newTemp := currentTemp + change

				minTemp := baseTemp - maxVar
				maxTemp := baseTemp + maxVar

				if newTemp < minTemp {
					newTemp = minTemp
				} else if newTemp > maxTemp {
					newTemp = maxTemp
				}
				currentTemp = newTemp

				p := influxdb2.NewPoint(
					"warehouse_temperature",
					map[string]string{
						"warehouse_id": fmt.Sprintf("%d", w.ID),
						"sector_id":    fmt.Sprintf("%d", s.ID),
					},
					map[string]interface{}{
						"temperature": newTemp,
					},
					t,
				)
				tempPoints = append(tempPoints, p)

				if len(tempPoints) >= 10000 {
					if err := writeAPI.WritePoint(context.Background(), tempPoints...); err != nil {
						log.Printf("Error writing temp batch: %v", err)
					} else {
						totalTempPoints += len(tempPoints)
					}
					tempPoints = nil
				}
			}
			if len(tempPoints) > 0 {
				if err := writeAPI.WritePoint(context.Background(), tempPoints...); err != nil {
					log.Printf("Error writing remaining temp points: %v", err)
				} else {
					totalTempPoints += len(tempPoints)
				}
			}
		}
	}

	log.Printf("Successfully seeded %d Temp points and %d Availability points into InfluxDB!", totalTempPoints, totalAvailabilityPoints)
}

func generateRandomSectorProfile() (float64, float64) {
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
