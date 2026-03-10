-- MANAGERS
CREATE OR REPLACE FUNCTION manager_search_vector_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.search_vector :=
        to_tsvector(
            'simple',
            coalesce(NEW.first_name,'') || ' ' ||
            coalesce(NEW.last_name,'') || ' ' ||
            coalesce(NEW.username,'') || ' ' ||
            coalesce(NEW.email,'')
        );
RETURN NEW;
END;
$$@@

DROP TRIGGER IF EXISTS trg_manager_search_vector ON manager@@

CREATE TRIGGER trg_manager_search_vector
BEFORE INSERT OR UPDATE ON manager
    FOR EACH ROW EXECUTE FUNCTION manager_search_vector_update()@@


-- PRODUCTS
CREATE OR REPLACE FUNCTION product_search_vector_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.search_vector :=
        to_tsvector(
            'simple',
            coalesce(NEW.name,'') || ' ' ||
            coalesce(NEW.description,'') || ' ' ||
            coalesce(NEW.sku,'')
        );
RETURN NEW;
END;
$$@@

DROP TRIGGER IF EXISTS trg_products_search_vector ON products@@

-- SKU generator
CREATE SEQUENCE IF NOT EXISTS seq_sku_soft_drinks START 1@@
CREATE SEQUENCE IF NOT EXISTS seq_sku_juices START 1@@
CREATE SEQUENCE IF NOT EXISTS seq_sku_water START 1@@
CREATE SEQUENCE IF NOT EXISTS seq_sku_sports_drinks START 1@@
CREATE SEQUENCE IF NOT EXISTS seq_sku_coffee START 1@@
CREATE SEQUENCE IF NOT EXISTS seq_sku_tea START 1@@
CREATE SEQUENCE IF NOT EXISTS seq_sku_plant_based START 1@@
CREATE SEQUENCE IF NOT EXISTS seq_sku_dairy START 1@@

CREATE TRIGGER trg_products_search_vector
BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION product_search_vector_update()@@

-- FACTORIES
CREATE OR REPLACE FUNCTION factory_search_vector_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.search_vector :=
        to_tsvector(
            'simple',
            coalesce(NEW.name,'') || ' ' ||
            coalesce(NEW.address,'')
        );
RETURN NEW;
END;
$$@@

DROP TRIGGER IF EXISTS trg_factories_search_vector ON factories@@

CREATE TRIGGER trg_factories_search_vector
BEFORE INSERT OR UPDATE ON factories
                        FOR EACH ROW EXECUTE FUNCTION factory_search_vector_update()@@

-- ============================================
-- WAREHOUSE SEARCH VECTOR FUNCTION
-- ============================================

CREATE OR REPLACE FUNCTION warehouse_search_vector_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.search_vector :=
    to_tsvector(
      'simple',
      coalesce(NEW.name,'') || ' ' ||
      coalesce(NEW.address,'')
    );
RETURN NEW;
END;
$$;

-- Drop existing trigger if exists
DROP TRIGGER IF EXISTS trg_warehouses_search_vector ON warehouses;

-- Create trigger that auto-updates search_vector
CREATE TRIGGER trg_warehouses_search_vector
    BEFORE INSERT OR UPDATE
                         ON warehouses
                         FOR EACH ROW
                         EXECUTE FUNCTION warehouse_search_vector_update();

-- ============================================
-- SECTOR SEARCH VECTOR
-- ============================================

CREATE OR REPLACE FUNCTION sector_search_vector_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.search_vector :=
    to_tsvector(
      'simple',
      coalesce(NEW.name,'') || ' ' ||
      coalesce(NEW.description,'')
    );
RETURN NEW;
END;
$$;

-- Add search_vector to sectors table
ALTER TABLE sectors
    ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Create index
CREATE INDEX IF NOT EXISTS idx_sectors_search
    ON sectors USING gin(search_vector);

-- Drop existing trigger if exists
DROP TRIGGER IF EXISTS trg_sectors_search_vector ON sectors;

-- Create trigger
CREATE TRIGGER trg_sectors_search_vector
    BEFORE INSERT OR UPDATE
                         ON sectors
                         FOR EACH ROW
                         EXECUTE FUNCTION sector_search_vector_update();

CREATE OR REPLACE FUNCTION company_search_vector_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.search_vector :=
    to_tsvector(
      'simple',
      coalesce(NEW.name,'') || ' ' ||
      coalesce(NEW.address,'')
    );
RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_companies_search_vector ON companies;

CREATE TRIGGER trg_companies_search_vector
    BEFORE INSERT OR UPDATE
                         ON companies
                         FOR EACH ROW
                         EXECUTE FUNCTION company_search_vector_update();

-- DELIVERY VEHICLE
CREATE OR REPLACE FUNCTION vehicle_search_vector_update()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.search_vector :=
        to_tsvector(
            'simple',
            coalesce(NEW.registration_number, '') || ' ' ||
            coalesce(NEW.weight_limit::text, '') || ' ' ||
            coalesce(NEW.is_online::text, '') || ' ' ||
            coalesce(NEW.last_latitude::text, '') || ' ' ||
            coalesce(NEW.last_longitude::text, '')
        );
RETURN NEW;
END;
$$;

-- Drop existing trigger if exists
DROP TRIGGER IF EXISTS trg_vehicle_search_vector ON vehicles;

-- Create trigger
CREATE TRIGGER trg_vehicle_search_vector
    BEFORE INSERT OR UPDATE
                         ON vehicles
                         FOR EACH ROW
                         EXECUTE FUNCTION vehicle_search_vector_update();