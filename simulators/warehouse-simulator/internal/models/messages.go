package models

type SectorTemperature struct {
	SectorID    int64   `json:"sectorId"`
	Temperature float64 `json:"temperature"`
}

type TemperatureMessage struct {
	WarehouseID  int64               `json:"warehouseId"`
	Timestamp    string              `json:"timestamp"`
	Temperatures []SectorTemperature `json:"temperatures"`
}

type HeartbeatMessage struct {
	WarehouseID int64  `json:"warehouseId"`
	Timestamp   string `json:"timestamp"`
	Status      string `json:"status"`
}
