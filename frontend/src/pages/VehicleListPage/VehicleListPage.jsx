import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import vehicleService from '../../services/vehicleService';
import './VehicleListPage.css';
import { mapVehicleFromApi } from "../../api/vehicle.mapper";
import SecureImage from '../../components/SecureImage';

const VehicleListPage = () => {
  const navigate = useNavigate();
  
  // State
  const [vehicles, setVehicles] = useState([]);
  const [makes, setMakes] = useState([]);
  const [models, setModels] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Pagination & Filters
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  
  const [searchQuery, setSearchQuery] = useState('');
  const [makeFilter, setMakeFilter] = useState(null);
  const [modelFilter, setModelFilter] = useState(null);
  const [sortBy, setSortBy] = useState('registrationNumber');
  const [sortDir, setSortDir] = useState('asc');

  // Load makes on mount
  useEffect(() => {
    loadMakes();
  }, []);

  // Load models when make is changed
  useEffect(() => {
    if (makeFilter) {
        loadModels(makeFilter);
    } else {
        setModels([]); // reset if no make selected
    }
  }, [makeFilter]);

  // Load vehicles when filters change
  useEffect(() => {
    loadVehicles();
  }, [page, pageSize, searchQuery, makeFilter, modelFilter, sortBy, sortDir]);

  const loadMakes = async () => {
    try {
      const response = await vehicleService.getMakes();
      setMakes(response.data);
    } catch (err) {
      console.error('Failed to load makes:', err);
    }
  };

  const loadModels = async (make) => {
    try {
      const response = await vehicleService.getModelsByMakeId(make);
      setModels(response.data);
    } catch (err) {
      console.error('Failed to load models:', err);
    }
  };

  const loadVehicles = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await vehicleService.getAllVehicles(
        page,
        pageSize,
        searchQuery,
        makeFilter,
        modelFilter,
        sortBy,
        sortDir
      );

      const mappedVehicles = response.data.content.map(mapVehicleFromApi);
      setVehicles(mappedVehicles);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (err) {
      console.error('Failed to load vehicles:', err);
      setError('Failed to load vehicles. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    setSearchQuery(e.target.value);
    setPage(0); // Reset to first page on search
  };

  const handleMakeChange = (e) => {
    if (e.target.value === "") {
      setMakeFilter(null);
      setModelFilter(null)
    } else {
      setMakeFilter(e.target.value);
      setModelFilter(null)
    }
    
    setPage(0);
  };

  const handleModelChange = (e) => {
    setModelFilter(e.target.value);
    setPage(0);
  };

  const handleSort = (column) => {
    if (sortBy === column) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortDir('asc');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this vehicle?')) {
      return;
    }

    try {
      await vehicleService.deleteVehicle(id);
      loadVehicles(); // Reload list
      alert('Vehicle deleted successfully!');
    } catch (err) {
      console.error('Failed to delete vehicle:', err);
      alert(err.response?.data?.message || 'Failed to delete vehicle.');
    }
  };

  return (
    <div className="vehicle-list-container">
      <div className="vehicle-list-header">
        <h1>Vehicles</h1>
        <button 
          className="btn-primary" 
          onClick={() => navigate('/vehicles/create')}
        >
          + Add New Vehicle
        </button>
      </div>

      {/* Filters */}
      <div className="filters-container">
        <div className="filter-group">
          <input
            type="text"
            placeholder="Search vehicles..."
            value={searchQuery}
            onChange={handleSearch}
            className="search-input"
          />
        </div>

        <div className="filter-group">
          <select 
            value={makeFilter} 
            onChange={handleMakeChange}
            className="filter-select"
          >
            <option value="">All Makes</option>
            {makes.map(make => (
              <option key={make.id} value={make.id}>
                {make.name}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <select 
            value={modelFilter} 
            onChange={handleModelChange}
            className="filter-select"
          >
            <option value="">All Models</option>
            {models.map(model => (
              <option key={model.id} value={model.id}>
                {model.name}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <select 
            value={pageSize} 
            onChange={(e) => {
              setPageSize(Number(e.target.value));
              setPage(0);
            }}
            className="filter-select"
          >
            <option value={10}>10 per page</option>
            <option value={25}>25 per page</option>
            <option value={50}>50 per page</option>
            <option value={100}>100 per page</option>
          </select>
        </div>
      </div>

      {/* Error Message */}
      {error && <div className="error-message">{error}</div>}

      {/* Loading State */}
      {loading ? (
        <div className="loading">Loading vehicles...</div>
      ) : (
        <>
          {/* Vehicles Table */}
          <div className="table-container">
            <table className="vehicles-table">
              <thead>
                <tr>
                  <th>Image</th>
                  <th onClick={() => handleSort('registrationNumber')} className="sortable">
                    Registration number {sortBy === 'registrationNumber' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th onClick={() => handleSort('make.name')} className="sortable">
                    Make {sortBy === 'make.name' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th onClick={() => handleSort('model.name')} className="sortable">
                    Model {sortBy === 'model.name' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th onClick={() => handleSort('weightLimit')} className="sortable">
                    Weight limit {sortBy === 'weightLimit' && (sortDir === 'asc' ? '↑' : '↓')}
                  </th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {vehicles.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="no-data">
                      No vehicles found. Create your first vehicle!
                    </td>
                  </tr>
                ) : (
                  vehicles.map(vehicle => (
                    <tr key={vehicle.id}>
                      <td>
                        <SecureImage 
                          imageUrl={vehicle.images[0] || '/static/vehicle.jpg'} 
                          altText={vehicle.registrationNumber}
                          className="vehicle-thumbnail"
                        />
                      </td>
                      <td>{vehicle.registrationNumber}</td>
                      <td>{vehicle.makeName}</td>
                      <td>{vehicle.modelName}</td>
                      <td>{vehicle.weightLimit} kg</td>
                      <td>
                        <span className={`vehicle-list-status-badge ${vehicle.isOnline ? 'vehicle-list-online' : 'vehicle-list-offline'}`}>
                          {vehicle.isOnline ? 'Online' : 'Offline'}
                        </span>
                      </td>
                      <td>
                        <div className="actions">
                            <button 
                          className="btn-view"
                          onClick={() => navigate(`/vehicles/${vehicle.id}`)}
                        >
                          View
                        </button>
                        <button 
                          className="btn-edit"
                          onClick={() => navigate(`/vehicles/${vehicle.id}/edit`)}
                        >
                          Edit
                        </button>
                        <button 
                          className="btn-delete"
                          onClick={() => handleDelete(vehicle.id)}
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

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button 
                onClick={() => setPage(0)} 
                disabled={page === 0}
                className="pagination-btn"
              >
                First
              </button>
              <button 
                onClick={() => setPage(page - 1)} 
                disabled={page === 0}
                className="pagination-btn"
              >
                Previous
              </button>
              
              <span className="pagination-info">
                Page {page + 1} of {totalPages} ({totalElements} total)
              </span>
              
              <button 
                onClick={() => setPage(page + 1)} 
                disabled={page >= totalPages - 1}
                className="pagination-btn"
              >
                Next
              </button>
              <button 
                onClick={() => setPage(totalPages - 1)} 
                disabled={page >= totalPages - 1}
                className="pagination-btn"
              >
                Last
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default VehicleListPage;
