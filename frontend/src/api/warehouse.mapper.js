export const mapWarehouseFromAPI = (warehouse) => {
  return {
    id: warehouse.id,
    name: warehouse.name,
    address: warehouse.address,
    city: warehouse.city,
    country: warehouse.country,
    latitude: warehouse.latitude,
    longitude: warehouse.longitude,
    imageUrls: warehouse.imageUrls || [],
    isOnline: warehouse.isOnline,
    lastHeartbeat: warehouse.lastHeartbeat,
    sectors: warehouse.sectors || [],
    totalSectors: warehouse.sectors?.length || 0,
    createdAt: warehouse.createdAt,
    updatedAt: warehouse.updatedAt,
    version: warehouse.version,
  };
};

export const mapWarehouseToAPI = (warehouse) => {
  return {
    name: warehouse.name,
    address: warehouse.address,
    cityId: warehouse.cityId,
    countryId: warehouse.countryId,
    latitude: parseFloat(warehouse.latitude),
    longitude: parseFloat(warehouse.longitude),
    sectors: warehouse.sectors || [],
  };
};
