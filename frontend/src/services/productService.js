import api from '../api/axios'

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const productService = {
  getAllProducts: (page = 0, size = 10, search = '', category = '', isAvailable = null, sortBy = 'name', sortDir = 'asc') => {
    const params = {
      page,
      size,
      sortBy,
      direction: sortDir,
    };
    
    if (search && search.trim()) params.search = search.trim();
    if (category) params.category = category;
    if (isAvailable !== null && isAvailable !== '') params.available = isAvailable;

    return api.get('/products', { params });
  },

  getProductById: (id) => {
    return api.get(`/products/${id}`);
  },

  createProduct: (productData, imageFile) => {
    const formData = new FormData();
    
    formData.append('data', new Blob([JSON.stringify({
      name: productData.name,
      description: productData.description,
      category: productData.category,
      price: productData.price,
      weight: productData.weight,
      is_available: productData.isAvailable ?? true,
      factoryIds: productData.factoryIds || [],
    })], { type: 'application/json' }));

    if (imageFile) {
      formData.append('image', imageFile);
    }

    return api.post('/products', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },


  updateProduct: (id, productData, imageFile) => {
    const formData = new FormData();
    
    const payload = {
        ...productData,
        is_available: productData.isAvailable,
    };
    delete payload.isAvailable;

    const jsonBlob = new Blob([JSON.stringify(payload)], { type: 'application/json' });
    formData.append('data', jsonBlob);

    if (imageFile) {
        formData.append('image', imageFile);
    }

    return api.put(`/products/${id}`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
  },

  deleteProduct: (id) => {
    return api.delete(`/products/${id}`);
  },

  getCategories: () => {
    return Promise.resolve({
      data: [
        { value: 'SOFT_DRINKS', label: 'Soft Drinks', description: 'Carbonated drinks like Coca-Cola, Sprite' },
        { value: 'JUICES', label: 'Juices', description: 'Fruit juices and nectars' },
        { value: 'WATER', label: 'Water', description: 'Various types of water' },
        { value: 'SPORTS_DRINKS', label: 'Sports & Energy Drinks', description: 'Energy and sports drinks' },
        { value: 'COFFEE', label: 'Coffee', description: 'Coffee and coffee products' },
        { value: 'TEA', label: 'Tea', description: 'Various teas' },
        { value: 'PLANT_BASED', label: 'Plant-Based Drinks', description: 'Plant-based and healthy beverages' },
        { value: 'DAIRY', label: 'Dairy Products', description: 'Yogurt, milk, etc.' },
      ]
    });
  },

  getAllFactoriesSimple: () => {
    return api.get('/factories/simple');
  },

  searchFactories: (query) => {
    return api.get('/factories/filter', {
      params: {
        query: query,
        size: 20 
      }
    });
  }
};

export default productService;
