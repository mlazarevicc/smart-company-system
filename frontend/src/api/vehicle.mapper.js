export const mapVehicleFromApi = (v) => ({
    id: v.id,
    registrationNumber: v.registrationNumber,
    weightLimit: v.weightLimit,
    makeId: v.makeId,
    modelId: v.modelId,
    makeName: v.makeName,
    modelName: v.modelName,
    images: v.images,

    lastHeartbeat: v.lastHeartbeat,
    isOnline: v.isOnline,
    lastLatitude: v.lastLatitude,
    lastLongitude: v.lastLongitude,
    lastLocationReadingAt: v.lastLocationReadingAt,

    version: v.version,
    createdAt: v.createdAt,
    updatedAt: v.updatedAt,
  });