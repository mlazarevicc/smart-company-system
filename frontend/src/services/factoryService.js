import api from '../api/axios';

const factoryService = {

  getFactories: (page = 0, size = 10, sort = 'createdAt,desc') => {
    return apiClient.get('/factories/filter', {
      params: { page, size, sort }
    });
  },

  searchFactories: (query, page = 0, size = 10, sort = 'createdAt,desc') => {
    return apiClient.get('/factories/filter', {
      params: { query, page, size, sort }
    });
  },
  
    filterFactories: ({
      query,
      countryId,
      cityId,
      online,
      productIds,
      page = 0,
      size = 10,
      sort = 'createdAt,desc',
    }) => {
      return api.get('/factories/filter', {
        params: {
          query: query || undefined,
          countryId: countryId || undefined,
          cityId: cityId || undefined,
          online: online !== null ? online : undefined,
          productIds: productIds && productIds.length ? productIds : undefined,
          page,
          size,
          sort,
        },
        paramsSerializer: (params) => {
          const usp = new URLSearchParams();
          Object.entries(params).forEach(([key, value]) => {
            if (value === undefined || value === null) return;
            if (Array.isArray(value)) {
              value.forEach((v) => usp.append(key, v));
            } else {
              usp.append(key, value);
            }
          });
          return usp.toString();
        },
      });
    },
  

  getFactoryById: (id) => {
    return api.get(`/factories/${id}`);
  },

  createFactory: (data, images = []) => {
    const formData = new FormData();

    formData.append(
      'data',
      new Blob(
        [
          JSON.stringify({
            name: data.name,
            address: data.address,
            cityId: data.cityId,
            countryId: data.countryId,
            latitude: data.latitude,
            longitude: data.longitude,
            productIds: data.productIds || []
          }),
        ],
        { type: 'application/json' }
      )
    );

    if (images && images.length > 0) {
      Array.from(images).forEach((img) => {
        formData.append('images', img);
      });
    }

    return api.post('/factories', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  updateFactory: (id, data, images) => {
    const formData = new FormData();
    
    const jsonBlob = new Blob([JSON.stringify({
        name: data.name,
        address: data.address,
        cityId: data.cityId,
        countryId: data.countryId,
        latitude: data.latitude,
        longitude: data.longitude,
        productIds: data.productIds || [],
        version: data.version
    })], { type: 'application/json' });

    formData.append('data', jsonBlob);

    if (images && images.length > 0) {
        images.forEach((image) => {
            formData.append('images', image);
        });
    }

    return api.put(`/factories/${id}`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
  },

  replaceFactoryImages: (id, images) => {
    const formData = new FormData();
    Array.from(images).forEach((img) => formData.append('images', img));

    return api.put(`/factories/${id}/images`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  deleteFactory: (id) => api.delete(`/factories/${id}`),

  getFactoriesSimple: () => api.get('/factories/simple'),

  getProductionAnalytics: (factoryId, params) => {
    return api.get(`/factories/${factoryId}/production-analytics`, { params });
  },

  getAvailabilityAnalytics: (id, params) => {
    // params očekuje: { period, fromDate, toDate }
    return api.get(`/factories/${id}/availability-analytics`, { params });
  },
};

export default factoryService;
