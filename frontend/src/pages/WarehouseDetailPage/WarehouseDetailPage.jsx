import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { 
  getWarehouseById, 
  deleteWarehouse,
  addSectorToWarehouse,
  deleteSectorFromWarehouse,
  getAvailabilityAnalytics,
  getWarehouseMetrics
} from '../../services/warehouseService';
import { mapWarehouseFromAPI } from '../../api/warehouse.mapper';
import websocketService from '../../services/webSocketService';
import './WarehouseDetailPage.css';
import Map from '../../components/Map/Map';
import { confirm } from '../../components/Dialog/ConfirmDialog';
import SecureImage from '../../components/SecureImage';
import WarehouseAvailabilityCharts from '../WarehousesAvailabilityCharts/WarehouseAvailabilityCharts';

const WarehouseDetailPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  
  const [warehouse, setWarehouse] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  
  const [newSector, setNewSector] = useState({ name: '', description: '' });
  const [showAddSector, setShowAddSector] = useState(false);
  const [addingSector, setAddingSector] = useState(false);
  const [expandedSector, setExpandedSector] = useState(null);
  
  const [temperatureData, setTemperatureData] = useState(null);
  const [wsConnected, setWsConnected] = useState(false);
  
  const [showDetailedAnalytics, setShowDetailedAnalytics] = useState(false);

  const subscriptionRef = useRef(null);
  const wsInitializedRef = useRef(false);

  useEffect(() => {
    loadWarehouse();
    loadAnalytics();
  }, [id]);

  useEffect(() => {
    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
      }
      websocketService.disconnect();
    };
  }, []);

useEffect(() => {
  if (!warehouse || wsInitializedRef.current) return;

  const token = localStorage.getItem('token');
  if (!token) return;

  wsInitializedRef.current = true;

  websocketService.connect(
    token,
    null,
    (error) => {
      setWsConnected(false);
    }
  );

  setTimeout(() => {
    setWsConnected(true);
    
    const sub = websocketService.subscribeToWarehouse(id, (data) => {
      setTemperatureData(data);
      
      if (data && data.sectorData && Array.isArray(data.sectorData)) {
        setWarehouse(prev => {
          if (!prev) return prev;
          
          return {
            ...prev,
            isOnline: data.isOnline ?? prev.isOnline,
            lastHeartbeat: data.lastHeartbeat ?? prev.lastHeartbeat,
            sectors: prev.sectors.map(sector => {
              const updatedSector = data.sectorData.find(
                s => s.sectorId === sector.id
              );
              
              if (updatedSector && updatedSector.currentTemperature !== null) {
                return {
                  ...sector,
                  lastTemperature: updatedSector.currentTemperature,
                  lastTemperatureReadingAt: updatedSector.lastReading || data.timestamp
                };
              }
              
              return sector;
            })
          };
        });
      } else {
        console.warn('⚠️ Invalid temperature data structure:', data);
      }
    });
    
    subscriptionRef.current = sub;
  }, 1000);

  return () => {
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
      subscriptionRef.current = null;
    }
    websocketService.disconnect();
    wsInitializedRef.current = false;
  };
}, [warehouse, id]);


  const loadWarehouse = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await getWarehouseById(id);
      const mappedWarehouse = mapWarehouseFromAPI(response);
      setWarehouse(mappedWarehouse);
    } catch (err) {
      console.error('Failed to load warehouse:', err);
      setError('Failed to load warehouse details.');
      toast.error('Failed to load warehouse details.');
    } finally {
      setLoading(false);
    }
  };

  const loadAnalytics = async () => {
  try {
    const metricsData = await getWarehouseMetrics(id);
    
    const endDate = new Date();
    const startDate = new Date(endDate.getTime() - 24 * 60 * 60 * 1000);
    
    let availabilityData = null;
    try {
       availabilityData = await getAvailabilityAnalytics(id, startDate, endDate, "1h");
    } catch (e) {
      console.warn('Failed to load availability analytics:', e);
    }

    setAnalytics({
      averageTemperature: metricsData.avgTemperature,
      uptimePercentage: availabilityData?.percentageOnline || 0,
      onlineTimeLastDay: availabilityData ? (availabilityData.totalOnlineMinutes / 60) : 0,
      totalReadings: availabilityData?.dataPoints?.length || 0
    });

  } catch (err) {
    console.error('Failed to load analytics:', err);
  }
};
  const handleAddSector = async () => {
    if (!newSector.name.trim()) {
      toast.error('Sector name is required.');
      return;
    }

    setAddingSector(true);
    try {
      const response = await addSectorToWarehouse(id, newSector);
      
      setWarehouse(prev => ({
        ...prev,
        sectors: [...prev.sectors, response],
        totalSectors: prev.totalSectors + 1
      }));
      
      setNewSector({ name: '', description: '' });
      setShowAddSector(false);
      toast.success('Sector added successfully!');
    } catch (err) {
      console.error('Failed to add sector:', err);
      toast.error('Failed to add sector.');
    } finally {
      setAddingSector(false);
    }
  };

const handleDeleteSector = async (sectorId) => {
  const ok = await confirm({
    title: 'Delete sector',
    message: 'Are you sure you want to delete this sector?',
    okText: 'Delete',
    cancelText: 'Cancel',
  });

  if (!ok) return;

  try {
    await deleteSectorFromWarehouse(id, sectorId);
    setWarehouse((prev) => ({
      ...prev,
      sectors: prev.sectors.filter((s) => s.id !== sectorId),
      totalSectors: prev.totalSectors - 1,
    }));
    toast.success('Sector deleted successfully!');
  } catch (err) {
    toast.error('Failed to delete sector.');
  }
};

const handleDeleteWarehouse = async () => {
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
    setTimeout(() => navigate('/warehouses'), 1500);
  } catch (err) {
    toast.error('Failed to delete warehouse.');
  }
};


  const nextImage = () => {
    if (warehouse && warehouse.imageUrls.length > 0) {
      setCurrentImageIndex((prev) => (prev + 1) % warehouse.imageUrls.length);
    }
  };

  const prevImage = () => {
    if (warehouse && warehouse.imageUrls.length > 0) {
      setCurrentImageIndex((prev) => 
        prev === 0 ? warehouse.imageUrls.length - 1 : prev - 1
      );
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="warehouse-detail-container">
        <div className="loading-skeleton">
          <div className="skeleton-header" />
          <div className="skeleton-content">
            <div className="skeleton-image" />
            <div className="skeleton-info">
              <div className="skeleton-line" />
              <div className="skeleton-line" />
              <div className="skeleton-line short" />
              <div className="skeleton-line" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !warehouse) {
    return (
      <div className="warehouse-detail-container">
        <div className="error-state">
          <h2>⚠️ Error Loading Warehouse</h2>
          <p>{error || 'Warehouse not found'}</p>
          <button className="btn-back" onClick={() => navigate('/warehouses')}>
            Back to Warehouses
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="warehouse-detail-container">
      <ToastContainer position="top-right" autoClose={3000} />

      <div className="breadcrumb">
        <span onClick={() => navigate('/warehouses')} className="breadcrumb-link">
          Warehouses
        </span>
        <span className="breadcrumb-separator">/</span>
        <span className="breadcrumb-current">{warehouse.name}</span>
      </div>

      <div className="warehouse-detail-header">
        <h1>{warehouse.name}</h1>
        <div className="header-actions">
          <button 
            className="btn-back" 
            onClick={() => navigate('/warehouses')}
          >
            ← Back
          </button>
        </div>
      </div>

      <div className="warehouse-detail-card">
        <div className="warehouse-detail-layout">
          <div className="warehouse-image-section">
<div className="warehouse-image-container">
  <SecureImage 
    imageUrl={
      (Array.isArray(warehouse.imageUrls) && warehouse.imageUrls.length > 0) 
        ? (warehouse.imageUrls[currentImageIndex] || '/static/product.jpg')
        : '/static/product.jpg'
    } 
    altText={`${warehouse.name} - Image ${currentImageIndex + 1}`} 
    className="warehouse-detail-image" 
  />


              {warehouse.imageUrls.length > 1 && (
                <>
                  <button className="image-nav-btn prev" onClick={prevImage}>
                    ‹
                  </button>
                  <button className="image-nav-btn next" onClick={nextImage}>
                    ›
                  </button>
                  <div className="image-counter">
                    {currentImageIndex + 1} / {warehouse.imageUrls.length}
                  </div>
                </>
              )}
            </div>

            <div className="warehouse-status">
              <span className={`status-badge ${warehouse.isOnline ? 'online' : 'offline'}`}>
                {warehouse.isOnline ? '🟢 ONLINE' : '🔴 OFFLINE'}
              </span>
              
              {wsConnected && (
                <div className="ws-indicator">
                  <span className="ws-dot" />
                  Real-time connected
                </div>
              )}
            </div>

            {warehouse.lastHeartbeat && (
              <div className="last-heartbeat">
                Last heartbeat: {formatDateTime(warehouse.lastHeartbeat)}
              </div>
            )}
          </div>

          <div className="warehouse-info-section">
            <div className="warehouse-header-info">
              <h2 className="warehouse-name">{warehouse.name}</h2>
              <p className="warehouse-address">
                📍 {warehouse.address}, {warehouse.city}, {warehouse.country}
              </p>
            </div>

            <div className="warehouse-details-grid">
              <div className="detail-item">
                <span className="detail-label">City</span>
                <span className="detail-value">{warehouse.city}</span>
              </div>

              <div className="detail-item">
                <span className="detail-label">Country</span>
                <span className="detail-value">{warehouse.country}</span>
              </div>

              <div className="detail-item">
                <span className="detail-label">Total Sectors</span>
                <span className="detail-value">
                  <span className="sectors-count">
                    {warehouse.totalSectors || warehouse.sectors?.length || 0}
                  </span>
                </span>
              </div>

              <div className="detail-item">
                <span className="detail-label">Status</span>
                <span className="detail-value">
                  {warehouse.isOnline ? 'Operational' : 'Offline'}
                </span>
              </div>

              {warehouse.manager && (
                <>
                  <div className="detail-item">
                    <span className="detail-label">Manager</span>
                    <span className="detail-value">
                      {warehouse.manager.firstName} {warehouse.manager.lastName}
                    </span>
                  </div>
                  
                  <div className="detail-item">
                    <span className="detail-label">Manager Email</span>
                    <span className="detail-value detail-email">
                      {warehouse.manager.email}
                    </span>
                  </div>
                </>
              )}
            </div>

            <div className="warehouse-actions">
              <button 
                className="btn-edit-action" 
                onClick={() => navigate(`/warehouses/${id}/edit`)}
              >
                Edit Warehouse
              </button>
              <button 
                className="btn-delete-action" 
                onClick={handleDeleteWarehouse}
              >
                Delete Warehouse
              </button>
            </div>
          </div>
        </div>

        {warehouse.latitude && warehouse.longitude && (
        <div className="map-section">
          <h3>📍 Location</h3>
          <Map
            latitude={warehouse.latitude}
            longitude={warehouse.longitude}
            name={warehouse.name}
            address={warehouse.address}
            city={warehouse.city}
            country={warehouse.country}
          />
        </div>
      )}

                {analytics && (
          <div className="warehouse-analytics">
            <div className="analytics-header">
              <h3>📊 Warehouse Analytics</h3>
              <button 
                className="btn-add-sector"
                onClick={() => setShowDetailedAnalytics(!showDetailedAnalytics)}
              >
                {showDetailedAnalytics ? 'Hide Detailed Analytics' : 'See Detailed Analytics'}
              </button>
            </div>
            
            {showDetailedAnalytics ? (
              <WarehouseAvailabilityCharts 
                warehouseId={id} 
                realtimeData={temperatureData} 
              />
            ) : (
              <div className="analytics-grid">
                <div className="metric-card">
                  <span className="metric-icon">🌡️</span>
                  <span className="metric-label">Avg Temperature</span>
                  <span className="metric-value">
                    {analytics.averageTemperature?.toFixed(1) || 'N/A'}°C
                  </span>
                </div>
                
                <div className="metric-card">
                  <span className="metric-icon">⏱️</span>
                  <span className="metric-label">Uptime</span>
                  <span className="metric-value">
                    {analytics.uptimePercentage?.toFixed(1) || '0'}%
                  </span>
                </div>
                <div className="metric-card">
                  <span className="metric-icon">📅</span>
                  <span className="metric-label">Online (24h)</span>
                  <span className="metric-value">
                    {analytics.onlineTimeLastDay?.toFixed(1) || '0'}h
                  </span>
                </div>
                
                <div className="metric-card">
                  <span className="metric-icon">📈</span>
                  <span className="metric-label">Total Readings</span>
                  <span className="metric-value">
                    {analytics.totalReadings || 0}
                  </span>
                </div>
              </div>
            )}
          </div>
        )}


        <div className="warehouse-sectors-section">
          <div className="sectors-header">
            <h3>🏭 Warehouse Sectors</h3>
            <button 
              className="btn-add-sector" 
              onClick={() => setShowAddSector(!showAddSector)}
            >
              {showAddSector ? '✕ Cancel' : '+ Add Sector'}
            </button>
          </div>

          {showAddSector && (
            <div className="add-sector-form">
              <div className="form-group">
                <label htmlFor="sector-name">Sector Name *</label>
                <input
                  id="sector-name"
                  type="text"
                  className="form-input"
                  placeholder="e.g., Sector A, Cold Storage 1"
                  value={newSector.name}
                  onChange={(e) => setNewSector({ ...newSector, name: e.target.value })}
                  disabled={addingSector}
                />
              </div>

              <div className="form-group">
                <label htmlFor="sector-description">Description</label>
                <textarea
                  id="sector-description"
                  className="form-textarea"
                  placeholder="Optional description..."
                  value={newSector.description}
                  onChange={(e) => setNewSector({ ...newSector, description: e.target.value })}
                  disabled={addingSector}
                />
              </div>

              <div className="form-actions-inline">
                <button
                  type="button"
                  className="btn-cancel-inline"
                  onClick={() => {
                    setShowAddSector(false);
                    setNewSector({ name: '', description: '' });
                  }}
                  disabled={addingSector}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn-submit-inline"
                  onClick={handleAddSector}
                  disabled={addingSector}
                >
                  {addingSector ? 'Adding...' : 'Add Sector'}
                </button>
              </div>
            </div>
          )}

          {warehouse.sectors && warehouse.sectors.length > 0 ? (
            <div className="sectors-list">
              {warehouse.sectors.map((sector) => (
                <div 
                  className={`sector-card ${expandedSector === sector.id ? 'expanded' : ''}`} 
                  key={sector.id}
                >
                  <div className="sector-card-header">
                    <h4 
                      className="sector-name"
                      onClick={() => setExpandedSector(
                        expandedSector === sector.id ? null : sector.id
                      )}
                    >
                      {sector.name}
                      <span className="expand-icon">
                        {expandedSector === sector.id ? '▼' : '▶'}
                      </span>
                    </h4>
                    <button
                      className="btn-delete-sector"
                      onClick={() => handleDeleteSector(sector.id)}
                      title="Delete sector"
                    >
                      🗑️
                    </button>
                  </div>

                  {sector.description && !expandedSector && (
                    <p className="sector-description-preview">{sector.description}</p>
                  )}

                  {sector.lastTemperature && (
                    <div className="sector-temperature">
                      <span className="temp-icon">🌡️</span>
                      <span className="temp-value">{sector.lastTemperature.toFixed(2)}°C</span>
                      {sector.lastTemperatureReadingAt && (
                        <span className="temp-time">
                          {formatDate(sector.lastTemperatureReadingAt)}
                        </span>
                      )}
                    </div>
                  )}

                  {expandedSector === sector.id && (
                    <div className="sector-expanded-content">
                      {sector.description && (
                        <div className="sector-detail">
                          <span className="sector-detail-label">Description:</span>
                          <p className="sector-description-full">{sector.description}</p>
                        </div>
                      )}

                      <div className="sector-stats">
                        <div className="sector-stat">
                          <span className="stat-icon">📅</span>
                          <span className="stat-label">Created:</span>
                          <strong>{formatDate(sector.createdAt)}</strong>
                        </div>

                        {sector.updatedAt && sector.updatedAt !== sector.createdAt && (
                          <div className="sector-stat">
                            <span className="stat-icon">🔄</span>
                            <span className="stat-label">Updated:</span>
                            <strong>{formatDate(sector.updatedAt)}</strong>
                          </div>
                        )}
                      </div>

                      <button
                        className="btn-view-sector"
                        onClick={() => navigate(`/warehouses/${id}/sectors/${sector.id}`)}
                      >
                        View Sector Details →
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="no-sectors">
              <p>No sectors defined for this warehouse yet.</p>
              <p className="no-sectors-hint">Click "Add Sector" to create one.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default WarehouseDetailPage;
