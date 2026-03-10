import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { 
  getAllWarehouses, 
  deleteWarehouse,
  getAllCountries 
} from '../../services/warehouseService'
import { mapWarehouseFromAPI } from '../../api/warehouse.mapper';
import './WarehouseListPage.css';
import { confirm } from '../../components/Dialog/ConfirmDialog';
import SecureImage from '../../components/SecureImage';

const WarehouseListPage = () => {
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [countryFilter, setCountryFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  
  // Pagination
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(16);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  
  // Dropdown data
  const [countries, setCountries] = useState([]);
  
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCountries = async () => {
      try {
        const data = await getAllCountries();
        setCountries(data);
      } catch (err) {
        console.error('Failed to fetch countries', err);
      }
    };
    fetchCountries();
  }, []);

  useEffect(() => {
    fetchWarehouses();
  }, [page, pageSize, searchQuery, countryFilter, statusFilter]);

  const fetchWarehouses = async () => {
    try {
      setLoading(true);
      
      const params = {
        page,
        size: pageSize,
      };
      
      if (searchQuery.trim()) {
        params.search = searchQuery.trim();
      }
      
      if (countryFilter) {
        params.country = countryFilter;
      }
      
      if (statusFilter) {
        params.status = statusFilter;
      }
      
      const response = await getAllWarehouses(params.page, params.size, params);
      
      const mappedWarehouses = response.content.map(mapWarehouseFromAPI);
      
      setWarehouses(mappedWarehouses);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
      setError(null);
    } catch (err) {
      setError('Failed to load warehouses');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

const handleDelete = async (id) => {
  const ok = await confirm({
    title: 'Delete warehouse',
    message: 'Are you sure you want to delete this warehouse? This action cannot be undone.',
    okText: 'Delete',
    cancelText: 'Cancel',
  });

  if (!ok) return;

  try {
    await deleteWarehouse(id);
    toast.success('Warehouse deleted successfully!');
    setTimeout(() => window.location.reload(), 500);
  } catch (err) {
    console.error('Failed to delete warehouse:', err);
    toast.error('Failed to delete warehouse.');
  }
};


  const resetFilters = () => {
    setSearchQuery('');
    setCountryFilter('');
    setStatusFilter('');
    setPage(0);
  };

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      if (page !== 0) {
        setPage(0);
      } else {
        fetchWarehouses();
      }
    }, 500);

    return () => clearTimeout(timeoutId);
  }, [searchQuery]);

  return (
    <div className="warehouse-list-page">
      <div className="warehouse-list-header">
        <h1>Warehouses</h1>
        <button 
          className="btn-add-new"
          onClick={() => navigate('/warehouses/create')}
        >
          + Add New Warehouse
        </button>
      </div>

      <div className="filters-container">
        <div className="filter-group">
          <input
            type="text"
            className="search-input"
            placeholder="Search warehouses..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="filter-group">
          <select 
            className="filter-select"
            value={countryFilter}
            onChange={(e) => {
              setCountryFilter(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All Countries</option>
            {countries.map(country => (
              <option key={country.id} value={country.name}>
                {country.name}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <select 
            className="filter-select"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All Status</option>
            <option value="online">Online</option>
            <option value="offline">Offline</option>
          </select>
        </div>

        <div className="filter-group">
          <select 
            className="filter-select"
            value={pageSize}
            onChange={(e) => {
              setPageSize(Number(e.target.value));
              setPage(0);
            }}
          >
            <option value="8">8 per page</option>
            <option value="16">16 per page</option>
            <option value="32">32 per page</option>
            <option value="64">64 per page</option>
          </select>
        </div>
      </div>

      {(searchQuery || countryFilter || statusFilter) && (
        <div className="active-filters">
          <span className="active-filters-label">Active filters:</span>
          {searchQuery && (
            <span className="filter-tag">
              Search: "{searchQuery}"
              <button onClick={() => setSearchQuery('')}>×</button>
            </span>
          )}
          {countryFilter && (
            <span className="filter-tag">
              Country: {countryFilter}
              <button onClick={() => setCountryFilter('')}>×</button>
            </span>
          )}
          {statusFilter && (
            <span className="filter-tag">
              Status: {statusFilter}
              <button onClick={() => setStatusFilter('')}>×</button>
            </span>
          )}
          <button className="clear-filters" onClick={resetFilters}>
            Clear all
          </button>
        </div>
      )}

      {error && <div className="error">{error}</div>}

      <div className="results-info">
        Showing {warehouses.length} of {totalElements} warehouses
      </div>

      {loading ? (
        <div className="loading-container">
          <div className="loading">Loading warehouses...</div>
        </div>
      ) : warehouses.length === 0 ? (
        <div className="empty-state">
          <h3>No warehouses found</h3>
          <p>Try adjusting your filters or add a new warehouse</p>
        </div>
      ) : (
        <div className="warehouse-grid">
          {warehouses.map((warehouse) => (
            <div key={warehouse.id} className="warehouse-card">
              <div className="warehouse-image">
                <SecureImage 
                  imageUrl={warehouse.imageUrls[0] || '/static/product.jpg'} 
                  altText={`${warehouse.name} - Image 1`} 
                  className="warehouse-detail-image" 
                />
                <span className={`status-badge ${warehouse.isOnline ? 'online' : 'offline'}`}>
                  {warehouse.isOnline ? 'ONLINE' : 'OFFLINE'}
                </span>
              </div>
              
              <div className="warehouse-info">
                <h3>{warehouse.name}</h3>
                <p className="location">
                  <span className="icon">📍</span>
                  {warehouse.city}, {warehouse.country}
                </p>
                <p className="address">{warehouse.address}</p>
                <p className="sectors">
                  <span className="icon">🏭</span>
                  {warehouse.totalSectors} sectors
                </p>
              </div>

              <div className="warehouse-actions">
                <button 
                  className="btn-view"
                  onClick={() => navigate(`/warehouses/${warehouse.id}`)}
                >
                  View
                </button>
                <button 
                  className="btn-edit"
                  onClick={() => navigate(`/warehouses/${warehouse.id}/edit`)}
                >
                  Edit
                </button>
                <button 
                  className="btn-delete"
                  onClick={() => handleDelete(warehouse.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && totalPages > 1 && (
        <div className="pagination">
          <button 
            className="pagination-btn"
            disabled={page === 0}
            onClick={() => setPage(0)}
          >
            First
          </button>
          <button 
            className="pagination-btn"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            Previous
          </button>
          <span className="pagination-info">
            Page {page + 1} of {totalPages} ({totalElements} total)
          </span>
          <button 
            className="pagination-btn"
            disabled={page >= totalPages - 1}
            onClick={() => setPage(page + 1)}
          >
            Next
          </button>
          <button 
            className="pagination-btn"
            disabled={page >= totalPages - 1}
            onClick={() => setPage(totalPages - 1)}
          >
            Last
          </button>
        </div>
      )}
      
      <ToastContainer />
    </div>
  );
};

export default WarehouseListPage;
