import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import factoryService from '../../services/factoryService';
import locationService from '../../services/locationService';
import './FactoryListPage.css';
import { confirm } from '../../components/Dialog/ConfirmDialog';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const FactoryListPage = () => {
  const navigate = useNavigate();

  const [factories, setFactories] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [selectedCountryId, setSelectedCountryId] = useState('');
  const [selectedCityId, setSelectedCityId] = useState('');
  const [onlineOnly, setOnlineOnly] = useState(false);

  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDirection, setSortDirection] = useState('desc');

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
    loadFactories();
  }, [page, pageSize, debouncedQuery, selectedCountryId, selectedCityId, onlineOnly, sortBy, sortDirection]);

  useEffect(() => {
    if (selectedCountryId) {
      loadCities(selectedCountryId);
    } else {
      setCities([]);
      setSelectedCityId('');
    }
  }, [selectedCountryId]);

  const loadCountries = async () => {
    try {
      const response = await locationService.getAllCountries();
      const data = Array.isArray(response) ? response : response.data;
      setCountries(Array.isArray(data) ? data : []); 
    } catch (err) {
      console.error('Failed to load countries', err);
      setCountries([]);
    }
  };

  const loadCities = async (countryId) => {
    try {
      const response = await locationService.getCityByCountry();
      const data = Array.isArray(response) ? response : response.data;
      setCities(Array.isArray(data) ? data : []); 
    } catch (err) {
      console.error('Failed to load countries', err);
      setCities([]);
    }
  };

  const loadFactories = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await factoryService.filterFactories({
        query: debouncedQuery,
        countryId: selectedCountryId,
        cityId: selectedCityId,
        online: onlineOnly ? true : null,
        page,
        size: pageSize,
        sort: `${sortBy},${sortDirection}`,
      });

      const data = response.data
      setFactories(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load factories');
    } finally {
      setLoading(false);
    }
  };

  const handlePrevPage = () => {
    if (page > 0) setPage(page - 1);
  };

  const handleNextPage = () => {
    if (page < totalPages - 1) setPage(page + 1);
  };

  const handleDelete = async (id) => {
    const ok = await confirm({
      title: 'Delete factory',
      message: 'Are you sure you want to delete this factory?',
      okText: 'Delete',
      cancelText: 'Cancel',
    });
  
    if (!ok) return;

    try {
      await factoryService.deleteFactory(id);
      toast.success('Factory deleted successfully!');
      setPage(0);
      // loadFactories();
      setTimeout(() => window.location.reload(), 500);
    } catch (err) {
      toast.alert('Failed to delete factory')
    }
  };

  const handleSort = (column) => {
    if (sortBy === column) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortDirection('asc');
    }
    setPage(0); 
  };

  const renderSortIndicator = (column) => {
    if (sortBy === column) {
      return sortDirection === 'asc' ? ' ▲' : ' ▼';
    }
    return <span style={{ opacity: 0.3 }}> ↕</span>;
  };

  return (
    <div className="factory-list-container">
      <div className="factory-list-header">
        <h1>Factories Management</h1>
        <button className="factory-list-btn-primary" onClick={() => navigate('/factories/create')}>
          + Add New Factory
        </button>
      </div>

      <div className="factory-list-filters-container">
        <div className="factory-list-filter-group-full">
          <input
            type="text"
            className="factory-list-search-input"
            placeholder="Search factories by name or address..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        
        <div className="factory-list-filter-row">
          <div className="factory-list-filter-group">
            <select
              className="factory-list-filter-select"
              value={selectedCountryId}
              onChange={(e) => setSelectedCountryId(e.target.value)}
            >
              <option value="">All Countries</option>
              {countries?.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div className="factory-list-filter-group">
            <select
              className="factory-list-filter-select"
              value={selectedCityId}
              onChange={(e) => setSelectedCityId(e.target.value)}
              disabled={!selectedCountryId}
            >
              <option value="">All Cities</option>
              {cities?.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div className="factory-list-filter-group">
    <label className="factory-list-checkbox-label factory-list-toggle">
      <input
        type="checkbox"
        checked={onlineOnly}
        onChange={(e) => setOnlineOnly(e.target.checked)}
      />
      <span className="factory-list-toggle-text">Online Only</span>
    </label>
  </div>

  {/* NOVI SELECT ZA PAGE SIZE */}
  <div className="factory-list-filter-group">
    <select 
      value={pageSize}
      onChange={(e) => {
        setPageSize(Number(e.target.value));
        setPage(0);
      }}
      className="factory-list-filter-select"
    >
      <option value={10}>10 per page</option>
      <option value={25}>25 per page</option>
      <option value={50}>50 per page</option>
      <option value={100}>100 per page</option>
    </select>
  </div>
        </div>
      </div>

      {loading && <div className="factory-list-loading">Loading...</div>}
      {error && <div className="factory-list-error-message">{error}</div>}

      {!loading && !error && (
        <>
          <div className="factory-list-table-responsive">
            <table className="factory-list-factory-table">
              <thead>
                <tr>
                  <th onClick={() => handleSort('name')} className="factory-list-sortable-header">
                    Name {renderSortIndicator('name')}
                  </th>
                  <th onClick={() => handleSort('city')} className="factory-list-sortable-header">
                    Location {renderSortIndicator('city')}
                  </th>
                  <th onClick={() => handleSort('isOnline')} className="factory-list-sortable-header">
                    Status {renderSortIndicator('isOnline')}
                  </th>
                  <th onClick={() => handleSort('createdAt')} className="factory-list-sortable-header">
                    Created {renderSortIndicator('createdAt')}
                  </th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {factories.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="factory-list-empty-message">No factories found.</td>
                  </tr>
                ) : (
                  factories.map((factory) => (
                    <tr key={factory.id}>
                      <td>
                        <strong>{factory.name}</strong>
                      </td>
                      <td>{factory.city}, {factory.country}</td>
                      <td>
                        <span className={`factory-list-status-badge ${factory.isOnline ? 'factory-list-online' : 'factory-list-offline'}`}>
                          {factory.isOnline ? 'Online' : 'Offline'}
                        </span>
                      </td>
                      <td>{new Date(factory.createdAt).toLocaleDateString()}</td>
                      <td>
                        <div className="factory-list-action-buttons">
                          <button
                            className="factory-list-btn-view"
                            onClick={() => navigate(`/factories/${factory.id}`)}
                          >
                            View
                          </button>
                          <button className="factory-list-btn-edit" onClick={() => navigate(`/factories/${factory.id}/edit`)}>Edit</button>
                          <button
                            className="factory-list-btn-delete"
                            onClick={() => handleDelete(factory.id)}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="factory-list-pagination">
              <button 
                onClick={() => setPage(0)} 
                disabled={page === 0}
                className="factory-list-pagination-btn"
              >
                First
              </button>
              <button 
                onClick={() => setPage(page - 1)} 
                disabled={page === 0}
                className="factory-list-pagination-btn"
              >
                Previous
              </button>
              
              <span className="factory-list-pagination-info">
                Page {page + 1} of {totalPages} ({totalElements} total)
              </span>
              
              <button 
                onClick={() => setPage(page + 1)} 
                disabled={page >= totalPages - 1}
                className="factory-list-pagination-btn"
              >
                Next
              </button>
              <button 
                onClick={() => setPage(totalPages - 1)} 
                disabled={page >= totalPages - 1}
                className="factory-list-pagination-btn"
              >
                Last
              </button>
            </div>
          )}
        </>
      )}
      <ToastContainer />
    </div>
  );
};

export default FactoryListPage;