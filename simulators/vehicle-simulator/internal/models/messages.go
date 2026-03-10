package models

import "time"

type DistanceMessage struct {
	VehicleID int64     `json:"vehicleId"`
	Timestamp time.Time `json:"timestamp"`
	Latitude  float64   `json:"latitude"`
	Longitude float64   `json:"longitude"`
	Distance  float64   `json:"distance"`
}

type HeartbeatMessage struct {
	VehicleID int64     `json:"vehicleId"`
	Timestamp time.Time `json:"timestamp"`
	Status    string    `json:"status"`
}
