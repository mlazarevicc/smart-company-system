import api from '../api/axios';

const orderService = {
  createOrder: (orderData) => {
    return api.post('/orders', orderData);
  },
  
    getCustomerCompanies: () => {
    return api.get('/companies/my-companies');
  },

  getMyOrders: async () => {
    const response = await api.get('/orders/my-orders'); 
    return response.data;
}
};

export default orderService;
