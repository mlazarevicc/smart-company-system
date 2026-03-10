package models

import "time"

type ProductionDetail struct {
	ProductID int64 `json:"productId"`
	Quantity  int64 `json:"quantity"`
}

type ProductionMessage struct {
	FactoryID   int64              `json:"factoryId"`
	Timestamp   time.Time          `json:"timestamp"`
	Productions []ProductionDetail `json:"productions"`
}

type HeartbeatMessage struct {
	FactoryID int64     `json:"factoryId"`
	Timestamp time.Time `json:"timestamp"`
	Status    string    `json:"status"`
}
