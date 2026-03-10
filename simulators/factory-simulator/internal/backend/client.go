package backend

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"time"

	"factory-simulator/internal/config"
)

type Client struct {
	baseURL    string
	httpClient *http.Client
}

type FactoryResponse struct {
	ID       int64             `json:"id"`
	Name     string            `json:"name"`
	Products []ProductResponse `json:"products"`
}

type ProductResponse struct {
	ProductID   int64 `json:"productId"`
	MinQuantity int64 `json:"minQuantity"`
	MaxQuantity int64 `json:"maxQuantity"`
}

func NewClient(baseURL string) *Client {
	return &Client{
		baseURL: baseURL,
		httpClient: &http.Client{
			Timeout: 30 * time.Second,
		},
	}
}

func (c *Client) FetchFactories(startID, endID int64) ([]config.FactoryConfig, error) {
	url := fmt.Sprintf("%s/api/factories/simulator-config?startId=%d&endId=%d",
		c.baseURL, startID, endID)

	log.Printf("📡 Fetching factory configs from: %s", url)

	resp, err := c.httpClient.Get(url)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch factories: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("backend returned status %d: %s", resp.StatusCode, string(body))
	}

	var factories []FactoryResponse
	if err := json.NewDecoder(resp.Body).Decode(&factories); err != nil {
		return nil, fmt.Errorf("failed to decode response: %w", err)
	}

	// Convert to internal config format
	configs := make([]config.FactoryConfig, len(factories))
	for i, factory := range factories {
		products := make([]config.ProductConfig, len(factory.Products))
		for j, product := range factory.Products {
			products[j] = config.ProductConfig{
				ProductID:   product.ProductID,
				MinQuantity: product.MinQuantity,
				MaxQuantity: product.MaxQuantity,
			}
		}

		configs[i] = config.FactoryConfig{
			ID:       factory.ID,
			Name:     factory.Name,
			Products: products,
		}
	}

	log.Printf("✅ Successfully loaded %d factory configurations", len(configs))

	return configs, nil
}
