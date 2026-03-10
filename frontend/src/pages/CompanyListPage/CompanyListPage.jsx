// src/pages/company/CompanyListPage.jsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import companyService from '../../services/companyService';
import locationService from '../../services/locationService';
import { mapCompanyFromApi } from '../../api/company.mapper';
import './CompanyListPage.css';
import SecureImage from '../../components/SecureImage';

const CompanyListPage = () => {
  const navigate = useNavigate();

  const [companies, setCompanies] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(10);

  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [selectedCountryId, setSelectedCountryId] = useState('');
  const [selectedCityId, setSelectedCityId] = useState('');
  const [onlineOnly, setOnlineOnly] = useState(false);

  const [countries, setCountries] = useState([]);
  const [cities, setCities] = useState([]);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedQuery(searchQuery.trim());
      setPage(0);
    }, 400);
    return () => clearTimeout(handler);
  }, [searchQuery]);

  useEffect(() => {
    loadCountries();
  }, []);

  useEffect(() => {
    loadCompanies();
  }, [page, debouncedQuery, selectedCountryId, selectedCityId, onlineOnly]);

  const loadCountries = async () => {
    try {
      const res = await locationService.getAllCountries(); // api.get('/locations')
      setCountries(res.data || []);
    } catch (err) {
      console.error('Failed to load countries:', err);
    }
  };

  const loadCitiesForCountry = async (countryId) => {
    if (!countryId) {
      setCities([]);
      setSelectedCityId('');
      return;
    }
    try {
      const res = await locationService.getCityByCountry(countryId); // api.get(`/locations/${id}/cities`)
      setCities(res.data || []);
    } catch (err) {
      console.error('Failed to load cities:', err);
      setCities([]);
    }
  };

  const loadCompanies = async () => {
    setLoading(true);
    setError('');

    try {
       const response = await companyService.filterCompanies({
        query: debouncedQuery || undefined,
        countryId: selectedCountryId ? Number(selectedCountryId) : undefined,
        cityId: selectedCityId ? Number(selectedCityId) : undefined,
        page,
        size: pageSize,
       });

      const data = response.data;
      const mapped = (data.content || []).map(mapCompanyFromApi);
      setCompanies(mapped);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      console.error('Failed to load companies:', err);
      setError('Failed to load companies. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
  };

  const handleCountryChange = async (e) => {
    const value = e.target.value;
    setSelectedCountryId(value);
    setSelectedCityId('');
    setPage(0);
    await loadCitiesForCountry(value);
  };

  const handleCityChange = (e) => {
    setSelectedCityId(e.target.value);
    setPage(0);
  };

  const handlePrevPage = () => {
    if (page > 0) setPage((prev) => prev - 1);
  };

  const handleNextPage = () => {
    if (page < totalPages - 1) setPage((prev) => prev + 1);
  };

  const handleCreate = () => {
    navigate('/companies/create');
  };

  return (
    <div className="company-list-container">
      <div className="company-list-header">
        <h1>Companies</h1>
        <button className="btn-primary" onClick={handleCreate}>
          + New company
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* Filters */}
      <div className="filters-container">
  {/* Search full width */}
  <div className=".filter-group-company .filter-group-company-full">
    <label className="filter-label">Search (name, address)</label>
    <input
      type="text"
      className="search-input"
      placeholder="Search companies..."
      value={searchQuery}
      onChange={handleSearchChange}
    />
  </div>

  {/* Country + City + Availability u jednom redu */}
  <div className="filter-row">
    <div className=".filter-group-company">
      <label className="filter-label">Country</label>
      <select
        className="filter-select"
        value={selectedCountryId}
        onChange={handleCountryChange}
      >
        <option value="">All countries</option>
        {countries.map((c) => (
          <option key={c.id} value={c.id}>
            {c.name}
          </option>
        ))}
      </select>
    </div>

    <div className=".filter-group-company">
      <label className="filter-label">City</label>
      <select
        className="filter-select"
        value={selectedCityId}
        onChange={handleCityChange}
        disabled={!selectedCountryId}
      >
        <option value="">All cities</option>
        {cities.map((city) => (
          <option key={city.id} value={city.id}>
            {city.name}
          </option>
        ))}
      </select>
    </div>

    {/*<div className=".filter-group-company filter-actions">
      <button
        type="button"
        className="filter-clear-btn"
        onClick={() => {
          setSearchQuery('');
          setSelectedCountryId('');
          setSelectedCityId('');
          setOnlineOnly(false);
          setPage(0);
        }}
      >
        Clear filters
      </button>
    </div>*/}
  </div>
</div>


      {loading ? (
        <div className="loading">Loading companies...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="companies-table">
              <thead>
                <tr>
                  <th>Image</th>
                  <th>Name</th>
                  <th>City / Country</th>
                  <th>Approval status</th>
                </tr>
              </thead>
              <tbody>
                {companies.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="no-data">
                      No companies found. Adjust filters or create your first company.
                    </td>
                  </tr>
                ) : (
                  companies.map((company) => (
                    <tr key={company.id}>
                      <td>
                        <SecureImage 
                          imageUrl={company.images[0] || '/static/company.jpg'} 
                          altText={company.name}
                          className="company-thumbnail"
                        />
                      </td>  
                      <td>{company.name}</td>
                      <td>
                        {company.city}, {company.country}
                      </td>
                      <td>
                        <span className={`status-badge ${company.status ? 'approved' : 'pending'}`}>
                          {company.status ? 'Approved' : 'Pending'}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <button
              className="pagination-btn"
              onClick={handlePrevPage}
              disabled={page === 0}
            >
              Previous
            </button>
            <span className="pagination-info">
              Page {page + 1} of {Math.max(totalPages, 1)} ({totalElements} results)
            </span>
            <button
              className="pagination-btn"
              onClick={handleNextPage}
              disabled={page >= totalPages - 1}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default CompanyListPage;
