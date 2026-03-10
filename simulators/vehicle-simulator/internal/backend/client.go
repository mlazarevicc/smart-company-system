package backend

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"vehicle-simulator/internal/config"
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

type VehicleResponse struct {
	ID                 int64  `json:"id"`
	RegistrationNumber string `json:"name"`
}

func (c *APIClient) FetchVehicles() ([]VehicleResponse, error) {
	url := fmt.Sprintf("%s/vehicles/simulator-config?startId=1&endId=1000", c.baseURL)

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

	var vehicles []VehicleResponse
	if err := json.Unmarshal(body, &vehicles); err != nil {
		return nil, fmt.Errorf("failed to unmarshal JSON: %w", err)
	}

	return vehicles, nil
}
