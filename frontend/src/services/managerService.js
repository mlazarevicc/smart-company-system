import api from '../api/axios'

export const registerManager = async (formData) => {
  const response = await api.post('/managers/register', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  return response.data;
};

export const getManagers = async ({ page, size, sortBy, direction, query, isBlocked }) => {
  const params = {
    page,
    size,
    sortBy,
    direction,
    q: query || undefined, 
    isBlocked: isBlocked !== undefined ? isBlocked : undefined
  };

  const response = await api.get('/managers', { params });
  return response.data;
};

export const getManagerById = async (id) => {
  const response = await api.get(`/managers/${id}`);
  return response.data;
};

export const toggleManagerBlock = async (id, blocked) => {
  const endpoint = blocked ? 'block' : 'unblock';
  const response = await api.put(`/managers/${id}/${endpoint}`, { blocked });
  return response.data;
};

export const changePassword = async (passwordData) => {
    const response = await api.post('/managers/change-password', passwordData);
    return response.data;
};
