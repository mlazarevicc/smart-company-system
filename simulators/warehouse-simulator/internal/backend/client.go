package backend

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"warehouse-simulator/internal/config"
)

type APIClient struct {
	baseURL string
	client  *http.Client
}

func NewAPIClient(cfg config.BackendConfig) *APIClient {
	return &APIClient{
		baseURL: cfg.APIURL,
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

type WarehouseResponse struct {
	ID      int64            `json:"id"`
	Name    string           `json:"name"`
	Sectors []SectorResponse `json:"sectors"`
}

type SectorResponse struct {
	ID   int64  `json:"id"`
	Name string `json:"name"`
}

func (c *APIClient) FetchWarehouses() ([]WarehouseResponse, error) {
	url := fmt.Sprintf("%s/warehouses/simulator-config?startId=1&endId=1000", c.baseURL)

	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	resp, err := c.client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to execute request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("backend returned status code %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %w", err)
	}

	var warehouses []WarehouseResponse
	if err := json.Unmarshal(body, &warehouses); err != nil {
		return nil, fmt.Errorf("failed to unmarshal JSON: %w", err)
	}

	return warehouses, nil
}
