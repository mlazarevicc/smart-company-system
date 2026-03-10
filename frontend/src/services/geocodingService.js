import api from '../api/axios';

const geocodingService = {
  geocodeFactoryAddress: (streetAddress, cityId) =>
    api.post('/geocode/factory-address', { streetAddress, cityId }),
  reverseGeocode: (latitude, longitude) =>
    api.post('/geocode/reverse', { latitude, longitude }),
};


export default geocodingService;
