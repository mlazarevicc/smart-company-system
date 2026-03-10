import api from '../api/axios';

// ===== WAREHOUSE CRUD =====

export const getAllWarehouses = async (page = 0, size = 20, filters = {}) => {
  const params = {
    page,
    size,
    ...filters
  };
  
  const response = await api.get('/warehouses', { params });
  return response.data;
};

export const getWarehouseById = async (id) => {
  const response = await api.get(`/warehouses/${id}`);
  console.log('Warehouse details:', response);
  return response.data;
};

export const createWarehouse = async (warehouseData, images) => {
  const formData = new FormData();
  formData.append('data', new Blob([JSON.stringify(warehouseData)], {
    type: 'application/json'
  }));

  if (images && images.length > 0) {
    images.forEach((image) => {
      formData.append('images', image);
    });
  }
  
  const response = await api.post('/warehouses', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  return response.data;
};


export const updateWarehouseWithImages = async (id, warehouseData, newFiles) => {
  const formData = new FormData();

  formData.append(
    'data',
    new Blob([JSON.stringify(warehouseData)], { type: 'application/json' })
  );

  (newFiles || []).forEach(file => {
    formData.append('images', file);
  });

  return api.put(`/warehouses/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};


export const deleteWarehouse = async (id) => {
  const response = await api.delete(`/warehouses/${id}`);
  return response.data;
};

export const recordHeartbeat = async (id) => {
  const response = await api.post(`/warehouses/${id}/heartbeat`);
  return response.data;
};

// ===== COUNTRIES & CITIES =====

export const getAllCountries = async () => {
  const response = await api.get('/locations');
  return response.data;
};

export const getCitiesByCountry = async (countryId) => {
  const response = await api.get(`/locations/${countryId}/cities`);
  return response.data;
};

// ===== SECTOR MANAGEMENT =====

export const getSectorById = async (warehouseId, sectorId) => {
  const response = await api.get(`/warehouses/${warehouseId}/sectors/${sectorId}`);
  return response.data;
};

export const getSectorTemperatureAnalytics = async (warehouseId, sectorId, startDate, endDate, granularity) => {
  const response = await api.get(
    `/warehouses/${warehouseId}/analytics/temperature`,
    { params: { sectorId, startDate, endDate, granularity } }
  );
  return response.data;
};


export const addSectorToWarehouse = async (warehouseId, sectorData) => {
  const response = await api.post(
    `/warehouses/${warehouseId}/sectors`,
    sectorData
  );
  return response.data;
};

export const updateSector = async (warehouseId, sectorId, sectorData) => {
  const response = await api.put(
    `/warehouses/${warehouseId}/sectors/${sectorId}`,
    sectorData
  );
  return response.data;
};

export const deleteSectorFromWarehouse = async (warehouseId, sectorId) => {
  const response = await api.delete(
    `/warehouses/${warehouseId}/sectors/${sectorId}`
  );
  return response.data;
};

// ===== ANALYTICS =====

export const getTemperatureAnalytics = async (
  warehouseId, 
  sectorId, 
  startDate, 
  endDate, 
  granularity = '15m'
) => {
  const response = await api.get(
    `/warehouses/${warehouseId}/analytics/temperature`,
    {
      params: {
        sectorId,
        startDate: startDate.toISOString(),
        endDate: endDate.toISOString(),
        granularity
      }
    }
  );
  return response.data;
};

export const getAvailabilityAnalytics = async (id, startDate, endDate, granularity) => {
  const start = startDate.toISOString();
  const end = endDate.toISOString();
  
  const response = await fetch(
    `/api/warehouses/${id}/analytics/availability?startDate=${start}&endDate=${end}&granularity=${granularity}`, 
    {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    }
  );
  
  if (!response.ok) throw new Error('Failed to fetch availability analytics');
  return response.json();
};

export const getCurrentMetrics = async (warehouseId) => {
  const response = await api.get(
    `/warehouses/${warehouseId}/metrics/current`
  );
  return response.data;
};

export const getWarehouseAnalytics = async (warehouseId) => {
  try {
    const metrics = await getCurrentMetrics(warehouseId);
    return metrics;
  } catch (error) {
    console.error('Failed to fetch analytics:', error);
    return null;
  }
};

export const getWarehouseMetrics = async (id) => {
  const response = await fetch(`/api/warehouses/${id}/metrics/current`, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  });
  
  if (!response.ok) throw new Error('Failed to fetch warehouse metrics');
  return response.json();
};
