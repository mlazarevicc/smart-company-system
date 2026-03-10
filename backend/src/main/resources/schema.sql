-- MANAGERS
ALTER TABLE manager ADD COLUMN IF NOT EXISTS search_vector tsvector@@

    CREATE INDEX IF NOT EXISTS idx_manager_search ON manager USING gin(search_vector)@@
    CREATE INDEX IF NOT EXISTS idx_manager_username ON manager (username)@@
    CREATE INDEX IF NOT EXISTS idx_manager_email ON manager (email)@@
    CREATE INDEX IF NOT EXISTS idx_manager_created_at ON manager (created_at)@@
    CREATE INDEX IF NOT EXISTS idx_manager_role_status ON manager (role, active, is_blocked)@@

-- PRODUCTS
ALTER TABLE products ADD COLUMN IF NOT EXISTS search_vector tsvector@@

    CREATE INDEX IF NOT EXISTS idx_products_search ON products USING gin(search_vector)@@
    CREATE INDEX IF NOT EXISTS idx_products_sku ON products (sku)@@
    CREATE INDEX IF NOT EXISTS idx_products_name ON products (name)@@
    CREATE INDEX IF NOT EXISTS idx_products_price ON products (price)@@
    CREATE INDEX IF NOT EXISTS idx_products_category ON products (category)@@
    CREATE INDEX IF NOT EXISTS idx_products_weight ON products (weight)@@
    CREATE INDEX IF NOT EXISTS idx_products_created_at ON products (created_at)@@
    CREATE INDEX IF NOT EXISTS idx_products_filtering ON products (is_deleted, is_available, category)@@

-- FACTORIES
ALTER TABLE factories ADD COLUMN IF NOT EXISTS search_vector tsvector@@

    CREATE INDEX IF NOT EXISTS idx_factories_search ON factories USING gin(search_vector)@@
    CREATE INDEX IF NOT EXISTS idx_factories_name ON factories (name)@@
    CREATE INDEX IF NOT EXISTS idx_factories_country_id ON factories (country_id)@@
    CREATE INDEX IF NOT EXISTS idx_factories_city_id ON factories (city_id)@@
    CREATE INDEX IF NOT EXISTS idx_factories_is_online ON factories (is_online)@@
    CREATE INDEX IF NOT EXISTS idx_factories_created_at ON factories (created_at)@@
    CREATE INDEX IF NOT EXISTS idx_product_factory_product_id ON product_factory (product_id)@@
    CREATE INDEX IF NOT EXISTS idx_product_factory_factory_id ON product_factory (factory_id)@@

-- ============================================
-- WAREHOUSE SEARCH VECTOR & INDEXES
-- ============================================

-- Add search_vector column to warehouses table
    ALTER TABLE warehouses
    ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Create GIN index for full-text search
CREATE INDEX IF NOT EXISTS idx_warehouses_search
    ON warehouses USING gin(search_vector);

-- Common filters/joins
CREATE INDEX IF NOT EXISTS idx_warehouses_country_id
    ON warehouses (country_id);

CREATE INDEX IF NOT EXISTS idx_warehouses_city_id
    ON warehouses (city_id);

CREATE INDEX IF NOT EXISTS idx_warehouses_is_online
    ON warehouses (is_online);

-- Warehouse images lookup
CREATE INDEX IF NOT EXISTS idx_warehouse_images_warehouse_id
ON warehouse_images (warehouse_id);

-- ============================================
-- SECTOR INDEXES
-- ============================================

-- Foreign key index for sectors
CREATE INDEX IF NOT EXISTS idx_sectors_warehouse_id
    ON sectors (warehouse_id);

-- Temperature lookup index
CREATE INDEX IF NOT EXISTS idx_sectors_last_temperature
    ON sectors (last_temperature);

-- Recent readings index
CREATE INDEX IF NOT EXISTS idx_sectors_last_reading_at
    ON sectors (last_temperature_reading_at);

ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS search_vector tsvector@@

    CREATE INDEX IF NOT EXISTS idx_companies_search
    ON companies USING gin(search_vector)@@

-- VEHICLES
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Create GIN index for full-text search on search_vector
    CREATE INDEX IF NOT EXISTS idx_vehicles_search
        ON vehicles USING gin(search_vector);

    -- Index on common filtering/joins
    CREATE INDEX IF NOT EXISTS idx_vehicles_registration_number
        ON vehicles (registration_number);

    CREATE INDEX IF NOT EXISTS idx_vehicles_weight_limit
        ON vehicles (weight_limit);

    CREATE INDEX IF NOT EXISTS idx_vehicles_is_online
        ON vehicles (is_online);

    CREATE INDEX IF NOT EXISTS idx_vehicles_created_by_manager_id
        ON vehicles (created_by_manager_id);

    CREATE INDEX IF NOT EXISTS idx_vehicles_last_latitude
        ON vehicles (last_latitude);

    CREATE INDEX IF NOT EXISTS idx_vehicles_last_longitude
        ON vehicles (last_longitude);

    CREATE INDEX IF NOT EXISTS idx_vehicles_last_heartbeat
        ON vehicles (last_heartbeat);

    CREATE INDEX IF NOT EXISTS idx_vehicles_created_at
        ON vehicles (created_at);

    CREATE INDEX IF NOT EXISTS idx_vehicles_updated_at
        ON vehicles (updated_at);
