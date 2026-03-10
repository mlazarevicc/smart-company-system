import api from '../api/axios';

const locationService = {
  searchCityCountry: (query, limit = 10) =>
    api.get('/locations/search', { params: { q: query, limit } }),

  getAllCountries: () => 
    api.get('/locations'),
  
  getCityByCountry: (id) =>
    api.get(`/locations/${id}/cities`)
};

export default locationService;
