import api from '../api/axios';

const companyService = {

  // GET /api/companies/filter
  filterCompanies: ({
    query,
    countryId,
    cityId,
    page = 0,
    size = 10,
    sort = 'createdAt,desc',
  }) => {
    return api.get('/companies/filter', {
      params: {
        query: query || undefined,
        countryId: countryId || undefined,
        cityId: cityId || undefined,
        page,
        size,
        sort,
      },
    });
  },

  getPendingCompanies: (page = 0, size = 20, sort = 'name') => {
    return api.get('/companies/pending', {
      params: { page, size, sort },
    })
  },

  // GET /api/companies (paginated for logged customer)
  getCompanies: (page = 0, size = 20, sort = 'name') => {
    return api.get('/companies', {
      params: { page, size, sort },
    });
  },

  // GET /api/companies/my-companies
  getMyCompanies: () => {
    return api.get('/companies/my-companies');
  },

  // GET /api/companies/{id}
  getCompanyById: (id) => {
    return api.get(`/companies/${id}`);
  },

  // POST /api/companies (multipart)
  createCompany: (data, images = [], ownershipFiles = []) => {
    const formData = new FormData();

    formData.append(
      'data',
      new Blob([JSON.stringify(data)], {
        type: 'application/json',
      })
    );

    if (images && images.length > 0) {
      Array.from(images).forEach((img) => {
        formData.append('images', img);
      });
    }

    if (ownershipFiles && ownershipFiles.length > 0) {
      Array.from(ownershipFiles).forEach((file) => {
        formData.append('proofOfOwnership', file);
      });
    }

    return api.post('/companies', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // PUT /api/companies/{id}/approve
  approveCompany: (id, data) => {
    return api.put(`/companies/${id}/approve`, data);
  },

  // PUT /api/companies/{id}/reject
  rejectCompany: (id, data) => {
    return api.put(`/companies/${id}/reject`, data);
  },
};

export default companyService;