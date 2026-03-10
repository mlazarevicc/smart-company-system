package main

import (
	"context"
	"fmt"
	"log"
	"math/rand"
	"time"

	"vehicle-simulator/internal/backend"
	"vehicle-simulator/internal/config"

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
	rand.Seed(time.Now().UnixNano())
	cfg, err := config.LoadConfig("config.yaml")
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	apiClient := backend.NewAPIClient(cfg.Backend)

	log.Println("Fetching vehicles from backend API...")
	backendVehicles, err := apiClient.FetchVehicles()
	if err != nil {
		log.Fatalf("Failed to fetch vehicles from backend. Is Spring Boot running? Error: %v", err)
	}

	if len(backendVehicles) == 0 {
		log.Println("No vehicles returned from backend API. Please create some in your database first.")
		return
	}

	client := influxdb2.NewClient(InfluxURL, InfluxToken)
	defer client.Close()

	writeAPI := client.WriteAPIBlocking(InfluxOrg, InfluxBucket)

	daysBack := 1826
	interval := 10 * time.Minute

	endTime := time.Now()
	startTime := endTime.AddDate(0, 0, -daysBack)

	log.Printf("Seeding data from %s to %s for %d vehicles...", startTime.Format(time.RFC3339), endTime.Format(time.RFC3339), len(backendVehicles))

	totalPoints := 0

	for _, w := range backendVehicles {
		log.Printf("Generating data for Vehicle: %s (ID: %d)", w.RegistrationNumber, w.ID)

		// Generate random values for initial distance (latitude and longitude are not needed)
		startDist := generateRandomLocationProfile()
		var points []*write.Point

		currentDist := startDist // Only track the distance

		for t := startTime; t.Before(endTime); t = t.Add(interval) {
			// Generate random change in distance (same as the simulator)
			changeDist := rand.Float64()*0.1 - 0.05

			currentDist += changeDist

			// Ensure distance does not go negative
			if currentDist < 0 {
				currentDist = 0 // Set the distance to 0 if it goes negative
			}
			// Now create a point for vehicle_distance measurement
			p := influxdb2.NewPoint(
				"vehicle_distance", // Measurement name
				map[string]string{
					"vehicle_id": fmt.Sprintf("%d", w.ID), // Tag: vehicle ID
				},
				map[string]interface{}{
					"distance_passed": currentDist, // Only the distance field
				},
				t,
			)
			points = append(points, p)

			// Write to InfluxDB in batches if needed
			if len(points) >= 10000 {
				if err := writeAPI.WritePoint(context.Background(), points...); err != nil {
					log.Printf("Error writing batch: %v", err)
				} else {
					totalPoints += len(points)
				}
				points = nil
			}
		}

		// Write remaining data
		if len(points) > 0 {
			if err := writeAPI.WritePoint(context.Background(), points...); err != nil {
				log.Printf("Error writing remaining points: %v", err)
			} else {
				totalPoints += len(points)
			}
		}

		if len(points) > 0 {
			if err := writeAPI.WritePoint(context.Background(), points...); err != nil {
				log.Printf("Error writing remaining points: %v", err)
			} else {
				totalPoints += len(points)
			}
		}
	}

	log.Printf("Successfully seeded %d data points into InfluxDB!", totalPoints)
}

func generateRandomLocationProfile() float64 {
	// Since you are only concerned with distance, we don't need lat/long
	// Start with a random distance
	dist := rand.Float64() * 1 // Initial random distance, can adjust as needed

	return dist // Return default lat/long (not needed anymore) and initial distance
}
