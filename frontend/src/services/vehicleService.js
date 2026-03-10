import api from '../api/axios'

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const makes = [
  { id: 1, name: "Toyota" },
  { id: 2, name: "Honda" },
  { id: 3, name: "Ford" },
  { id: 4, name: "Chevrolet" },
  { id: 5, name: "BMW" },
  { id: 6, name: "Mercedes-Benz" },
  { id: 7, name: "Audi" },
  { id: 8, name: "Nissan" }
];

const models = [
  // Toyota (1)
  { id: 1, name: "Corolla", makeId: 1 },
  { id: 2, name: "Camry", makeId: 1 },
  { id: 3, name: "RAV4", makeId: 1 },

  // Honda (2)
  { id: 4, name: "Civic", makeId: 2 },
  { id: 5, name: "Accord", makeId: 2 },
  { id: 6, name: "CR-V", makeId: 2 },

  // Ford (3)
  { id: 7, name: "F-150", makeId: 3 },
  { id: 8, name: "Mustang", makeId: 3 },
  { id: 9, name: "Explorer", makeId: 3 },

  // Chevrolet (4)
  { id: 10, name: "Silverado", makeId: 4 },
  { id: 11, name: "Malibu", makeId: 4 },
  { id: 12, name: "Equinox", makeId: 4 },

  // BMW (5)
  { id: 13, name: "3 Series", makeId: 5 },
  { id: 14, name: "5 Series", makeId: 5 },
  { id: 15, name: "X5", makeId: 5 },

  // Mercedes-Benz (6)
  { id: 16, name: "C-Class", makeId: 6 },
  { id: 17, name: "E-Class", makeId: 6 },
  { id: 18, name: "GLE", makeId: 6 },

  // Audi (7)
  { id: 19, name: "A4", makeId: 7 },
  { id: 20, name: "Q5", makeId: 7 },
  { id: 21, name: "A6", makeId: 7 },

  // Nissan (8)
  { id: 22, name: "Altima", makeId: 8 },
  { id: 23, name: "Sentra", makeId: 8 },
  { id: 24, name: "Rogue", makeId: 8 }
];

const vehicleService = {
  /**
   * Get all vehicles with pagination, search, and filters
   */

  
  getAllVehicles: (page = 0, size = 10, search = '', make = null, model = null, sortBy = 'registrationNumber', sortDir = 'asc') => {
    const params = {
      page,
      size,
      sortBy,
      direction: sortDir,
    };

    console.log(params)
    
    if (search && search.trim()) params.search = search.trim();
    if (make) params.makeId = make;
    if (model) params.modelId = model;

    console.log(params)

    return api.get('/vehicles', { params });
  },


  /**
   * Get vehicle by ID
   */
  getVehicleById: (id) => {
    return api.get(`/vehicles/${id}`);
  },

  /**
   * Create new vehicle
   */
  createVehicle: (vehicleData, imageFiles) => {
    const formData = new FormData();
    
    // Append vehicle data as JSON blob
    formData.append('data', new Blob([JSON.stringify({
      registrationNumber: vehicleData.registrationNumber,
      weightLimit: vehicleData.weightLimit,
      makeId: vehicleData.make,
      modelId: vehicleData.model,
    })], { type: 'application/json' }));

    // Append image file if provided
    if (imageFiles && imageFiles.length > 0) {
      imageFiles.forEach((image) => {
        formData.append('image', image);
      });
    }

    return api.post('/vehicles', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * Update existing vehicle
   */
  updateVehicle: (id, vehicleData, imageFiles) => {
    const formData = new FormData();
    const payload = {
      version: vehicleData.version,
      registrationNumber: vehicleData.registrationNumber,
      weightLimit: vehicleData.weightLimit,
      makeId: vehicleData.make,
      modelId: vehicleData.model,
    }
    console.log(payload)
    formData.append('data', new Blob([JSON.stringify(payload)], {type: 'application/json'}));

    (imageFiles || []).forEach(file => {
      formData.append('images', file);
    });
    
    return api.put(`/vehicles/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * Update vehicle image
   */
  updateVehicleImage: (id, imageFiles) => {
    const formData = new FormData();
    formData.append('image', imageFiles);

    return api.put(`/vehicles/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * Delete vehicle
   */
  deleteVehicle: (id) => {
    return api.delete(`/vehicles/${id}`);
  },

  /**
   * Get all vehicle makes
   */
  getMakes: () => {
    return api.get(`/vehicles/makes`);
  },

  /**
   * Get vehicle models depending on vehicle make
   */
  getModelsByMakeId: (makeId) => {
    return api.get(`/vehicles/make/${makeId}/models`)
  },

  getCurrentMetrics: (vehicleId) => {
    return api.get(`/vehicles/${vehicleId}/metrics/current`);
  },

  getAvailabilityAnalytics: (vehicleId, params) => {
    return api.get(`/vehicles/${vehicleId}/analytics/availability`, { params });
},

  getDistanceAnalytics: (vehicleId, startDate, endDate, granularity = '1h') => {
  // Format dates to ISO string if necessary
  const start = startDate;
  const end = endDate;

  return api.get(`/vehicles/${vehicleId}/analytics/distance`, {
    params: {
      startDate: start,
      endDate: end,
      granularity: granularity
    }
  });
},
};

export default vehicleService;