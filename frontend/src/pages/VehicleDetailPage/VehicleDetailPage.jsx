import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import vehicleService from '../../services/vehicleService';
import './VehicleDetailPage.css';
import { mapVehicleFromApi } from "../../api/vehicle.mapper";
import Map from '../../components/Map/Map';
import SecureImage from '../../components/SecureImage';
import VehicleAvailabilityDashboard from '../VehicleAvailabilityDashboard/VehicleAvailabilityDashboard';
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

const VehicleDetailPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  
  const [vehicle, setVehicle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const [selectedPeriod, setSelectedPeriod] = useState('7d');
  const [customFrom, setCustomFrom] = useState('');
  const [customTo, setCustomTo] = useState('');
  const [customError, setCustomError] = useState('');

  const [analyticsData, setAnalyticsData] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
  
  const chartData = Object.values(
    (analyticsData?.dataPoints ?? []).reduce((acc, p) => {
      const rawTimestamp = p.timestamp.endsWith('Z') ? p.timestamp : `${p.timestamp}Z`;
      const dateObj = new Date(rawTimestamp);
      const dateKey = dateObj.toLocaleDateString('en-GB', { day: '2-digit', month: 'short' });

      acc[dateKey] = {
        timestamp: rawTimestamp,
        distance: p.distance,
        formattedDate: dateKey,
        fullDate: dateObj.toLocaleString('en-GB', { 
          day: '2-digit', month: 'short', year: 'numeric',
          hour: '2-digit', minute: '2-digit' 
        }),
        unixTime: dateObj.getTime() 
      };
      return acc;
    }, {})
  ).sort((a, b) => a.unixTime - b.unixTime);

  const PERIOD_OPTIONS = [
    { label: 'Last 7 days',  value: '7d',   days: 7   },
    { label: 'Last 30 days', value: '30d',  days: 30  },
    { label: '3 months',     value: '90d',  days: 90  },
    { label: '6 months',     value: '180d', days: 180 },
    { label: '1 year',       value: '365d', days: 365 },
    { label: 'Custom',       value: 'custom'          },
  ];

  const getGranularity = (days) => {
    if (days <= 30)  return '1d';
    if (days <= 90)  return '1w';
    return '30d';
  };

  useEffect(() => {
    loadVehicle();
  }, [id]);

  useEffect(() => {
  if (!vehicle?.id) return;

  if (selectedPeriod !== 'custom') {
    loadAnalytics();
  }
}, [selectedPeriod, vehicle]);

  function toISOLocal(d) {
      var z  = n =>  ('0' + n).slice(-2);
      var zz = n => ('00' + n).slice(-3);
      var off = d.getTimezoneOffset();
      var sign = off > 0? '-' : '+';
      off = Math.abs(off);

      return d.getFullYear() + '-'
            + z(d.getMonth()+1) + '-' +
            z(d.getDate()) + 'T' +
            z(d.getHours()) + ':'  + 
            z(d.getMinutes()) + ':' +
            z(d.getSeconds()) + '.' +
            zz(d.getMilliseconds()) +
            sign + z(off/60|0) + ':' + z(off%60); 
    }

  const handleCustomSubmit = () => {
    setCustomError('');

    if (!customFrom || !customTo) {
      setCustomError('Please select both start and end date.');
      return;
    }

    const from = new Date(customFrom);
    const to = new Date(customTo);

    if (from >= to) {
      setCustomError('Start date must be before end date.');
      return;
    }

    const diffDays = Math.ceil((to - from) / (1000 * 60 * 60 * 24));
    if (diffDays > 365) {
      setCustomError('Date range cannot exceed 1 year.');
      return;
    }

    loadAnalytics(customFrom, customTo, diffDays);
  };

  const loadVehicle = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await vehicleService.getVehicleById(id);
      const mappedVehicle = mapVehicleFromApi(response.data)

      setVehicle(mappedVehicle);
    } catch (err) {
      console.error('Failed to load vehicle:', err);
      setError('Failed to load vehicle details.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this vehicle? This action cannot be undone.')) {
      return;
    }

    try {
      await vehicleService.deleteVehicle(id);
      alert('Vehicle deleted successfully!');
      navigate('/vehicles');
    } catch (err) {
      console.error('Failed to delete vehicle:', err);
      alert(err.response?.data?.message || 'Failed to delete vehicle.');
    }
  };

  const nextImage = () => {
    if (vehicle && vehicle.images.length > 0) {
      setCurrentImageIndex((prev) => (prev + 1) % vehicle.images.length);
    }
  };

  const prevImage = () => {
    if (vehicle && vehicle.images.length > 0) {
      setCurrentImageIndex((prev) => 
        prev === 0 ? vehicle.images.length - 1 : prev - 1
      );
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const loadAnalytics = async (fromDate, toDate, days) => {  
      setAnalyticsLoading(true);
      try {
        const now = new Date();
        let start, end, diffDays;
  
        if (fromDate && toDate) {
          start = new Date(fromDate);
          end = new Date(toDate);
          diffDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        } else {
          const option = PERIOD_OPTIONS.find((p) => p.value === selectedPeriod);
          diffDays = option?.days || 7;
          end = now;
          start = new Date(now);
          start.setDate(start.getDate() - diffDays);
        }
  
        const granularity = getGranularity(diffDays);
  
        const response = await vehicleService.getDistanceAnalytics(
          vehicle.id,
          toISOLocal(start),
          toISOLocal(end),
          granularity
        );
  
        setAnalyticsData(response.data);   
      } catch (err) {
        console.error('Failed to load analytics:', err);
        toast.error('Failed to load analytics data.');
      } finally {
        setAnalyticsLoading(false);
      }
    };

  if (loading) {
    return (
      <div className="vehicle-detail-container">
        <div className="loading">Loading vehicle details...</div>
      </div>
    );
  }

  if (error || !vehicle) {
    return (
      <div className="vehicle-detail-container">
        <div className="error-message">{error || 'Vehicle not found.'}</div>
        <button className="btn-back" onClick={() => navigate('/vehicles')}>
          ← Back to Vehicles
        </button>
      </div>
    );
  }

  return (
    <div className="vehicle-detail-container">
      <div className="vehicle-detail-header">
        <h1>Vehicle Details</h1>
        <button className="btn-back" onClick={() => navigate('/vehicles')}>
          ← Back to Vehicles
        </button>
      </div>

      <div className="vehicle-detail-card">
        <div className="vehicle-detail-layout">
          {/* Left Side - Image */}
          <div className="vehicle-image-section">
            <div className="vehicle-image-container">
              <SecureImage
                imageUrl={vehicle.images[currentImageIndex] || '/static/vehicle.jpg'}
                altText={`${vehicle.registrationNumber} - Image ${currentImageIndex + 1}`}
                className="vehicle-detail-image"
              />
              
              {vehicle.images.length > 1 && (
                <>
                  <button className="image-nav-btn prev" onClick={prevImage}>
                    ‹
                  </button>
                  <button className="image-nav-btn next" onClick={nextImage}>
                    ›
                  </button>
                  <div className="image-counter">
                    {currentImageIndex + 1} / {vehicle.images.length}
                  </div>
                </>
              )}
            </div>
          </div>

          {/* Right Side - Details */}
          <div className="vehicle-info-section">
            <div className="vehicle-header-info">
              <h2 className="vehicle-name">{vehicle.registrationNumber}</h2>
            </div>

            <div className="vehicle-details-grid">
              {/* Make */}
              <div className="detail-item">
                <span className="detail-label">Make</span>
                <span className="detail-value">
                  {vehicle.makeName}
                </span>
              </div>

              {/* Model */}
              <div className="detail-item">
                <span className="detail-label">Model</span>
                <span className="detail-value">
                  {vehicle.modelName}
                </span>
              </div>

              {/* Weight */}
              <div className="detail-item">
                <span className="detail-label">Weight Limit</span>
                <span className="detail-value">
                  {Number(vehicle.weightLimit).toFixed(3)} kg
                </span>
              </div>

              {/* Version */}
              <div className="detail-item">
                <span className="detail-label">Version</span>
                <span className="detail-value">
                  v{vehicle.version}
                </span>
              </div>

              {/* Created At */}
              <div className="detail-item">
                <span className="detail-label">Created</span>
                <span className="detail-value">
                  {formatDate(vehicle.createdAt)}
                </span>
              </div>

              {/* Updated At */}
              <div className="detail-item">
                <span className="detail-label">Last Updated</span>
                <span className="detail-value">
                  {formatDate(vehicle.updatedAt)}
                </span>
              </div>

              <div className="detail-item">
                <span className="detail-label">Status</span>
                <span className="detail-value">
                  <span className={`vehicle-list-status-badge ${vehicle.isOnline ? 'vehicle-list-online' : 'vehicle-list-offline'}`}>
                      {vehicle.isOnline ? 'Online' : 'Offline'}
                  </span>
                </span>
              </div>
            </div>

            

            {/* Actions */}
            <div className="vehicle-actions">
              <button 
                className="btn-edit"
                onClick={() => navigate(`/vehicles/${id}/edit`)}
              >
                Edit Vehicle
              </button>
              <button 
                className="btn-delete"
                onClick={handleDelete}
              >
                Delete Vehicle
              </button>
            </div>
          </div>
        </div>
      </div>
      <div className="vehicle-detail-container">
        <div className="vehicle-detail-card">
          {vehicle.lastLatitude && vehicle.lastLongitude && (
            <div className="map-section">
              <h3>📍 Last known location</h3>
              <Map
                latitude={vehicle.lastLatitude}
                longitude={vehicle.lastLongitude}
                name={vehicle.registrationNumber}
                address={vehicle.lastLocationReadingAt}
              />
            </div>
          )}
        </div>
      </div>
      <VehicleAvailabilityDashboard vehicleId={id} />

      <div className="sector-analytics-card">
              <h2>📈 Distance Analytics</h2>
      
              <div className="period-selector">
                {PERIOD_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    type="button"
                    className={`period-btn ${selectedPeriod === opt.value ? 'active' : ''}`}
                    onClick={() => setSelectedPeriod(opt.value)}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
      
              {selectedPeriod === 'custom' && (
                <div className="custom-range">
                  <div className="custom-range-inputs">
                    <div className="date-input-group">
                      <label>From</label>
                      <input
                        type="date"
                        value={customFrom}
                        onChange={(e) => setCustomFrom(e.target.value)}
                        max={customTo || undefined}
                        className="date-input"
                      />
                    </div>
                    <div className="date-input-group">
                      <label>To</label>
                      <input
                        type="date"
                        value={customTo}
                        onChange={(e) => setCustomTo(e.target.value)}
                        min={customFrom || undefined}
                        max={toISOLocal(new Date()).split('T')[0]}
                        className="date-input"
                      />
                    </div>
                    <button
                      type="button"
                      className="btn-apply"
                      onClick={handleCustomSubmit}
                    >
                      Apply
                    </button>
                  </div>
                  {customError && <p className="custom-error">{customError}</p>}
                </div>
              )}
      
              {selectedPeriod !== 'custom' && (
                <p className="granularity-hint">
                  {selectedPeriod === '7d' || selectedPeriod === '30d'
                    ? '📅 Showing daily averages'
                    : selectedPeriod === '90d' || selectedPeriod === '180d'
                    ? '📅 Showing weekly averages'
                    : '📅 Showing monthly averages'}
                </p>
              )}
      
              <div className="chart-container">
                  {analyticsLoading ? (
                      <div className="loading-chart">Loading analytics...</div>
                  ) : chartData.length ? (
                      <ResponsiveContainer width="100%" height={360}>
                        <LineChart data={chartData} margin={{ top: 10, right: 30, bottom: 20, left: 10 }}>
                          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                          
                          <XAxis 
                            dataKey="formattedDate"
                            axisLine={false}
                            tickLine={false}
                            tick={{ fill: '#64748b', fontSize: 12 }}
                            dy={10}
                          />
      
                          <YAxis 
                            unit="km" 
                            axisLine={false}
                            tickLine={false}
                            tick={{ fill: '#64748b', fontSize: 12 }}
                            width={45}
                            domain={['auto', 'auto']}
                          />
                          
                          <Tooltip 
                            labelFormatter={(label) => label} 
                            formatter={(val) => [`${val} km`, 'Distance passed']}
                            contentStyle={{ 
                              borderRadius: '8px', 
                              border: 'none', 
                              boxShadow: '0 4px 15px rgba(0,0,0,0.1)' 
                            }}
                          />
                          
                          <Line 
                            type="monotone" 
                            dataKey="distance" 
                            stroke="#6366f1" 
                            strokeWidth={3}
                            dot={{ fill: '#6366f1', strokeWidth: 2, r: 4 }}
                            activeDot={{ r: 6, strokeWidth: 0 }}
                            animationDuration={1000}
                          />
                        </LineChart>
                      </ResponsiveContainer>
      
                  ) : (
                      <div className="no-data">No data available for selected period.</div>
                  )}
              </div>
            </div>
    </div>
        
  );
};

export default VehicleDetailPage;
