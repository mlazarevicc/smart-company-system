import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { getSectorById, getSectorTemperatureAnalytics, updateSector } from '../../services/warehouseService';
import './SectorDetailPage.css';
import {
  ResponsiveContainer, LineChart, Line,
  XAxis, YAxis, CartesianGrid, Tooltip
} from 'recharts';

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

const SectorDetailPage = () => {
  const navigate = useNavigate();
  const { warehouseId, id } = useParams();

  const [sector, setSector] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [selectedPeriod, setSelectedPeriod] = useState('7d');
  const [customFrom, setCustomFrom] = useState('');
  const [customTo, setCustomTo] = useState('');
  const [customError, setCustomError] = useState('');

  const [analyticsData, setAnalyticsData] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);

  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({ name: '', description: '', version: null });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const chartData = Object.values(
    (analyticsData?.dataPoints ?? []).reduce((acc, p) => {
      const rawTimestamp = p.timestamp.endsWith('Z') ? p.timestamp : `${p.timestamp}Z`;
      const dateObj = new Date(rawTimestamp);
      const dateKey = dateObj.toLocaleDateString('en-GB', { day: '2-digit', month: 'short' });

      acc[dateKey] = {
        timestamp: rawTimestamp,
        temperature: p.temperature,
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

  useEffect(() => {
    loadSector();
  }, [id]);

  useEffect(() => {
    if (sector && selectedPeriod !== 'custom') {
      loadAnalytics();
    }
  }, [sector, selectedPeriod]);

  const loadSector = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getSectorById(warehouseId, id);
      setSector(data);
    } catch (err) {
      console.error('Failed to load sector:', err);
      setError('Failed to load sector details.');
      toast.error('Failed to load sector details.');
    } finally {
      setLoading(false);
    }
  };

  const loadAnalytics = async (fromDate, toDate, days) => {
    if (!sector) return;

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

      const data = await getSectorTemperatureAnalytics(
        sector.warehouseId,
        sector.id,
        toISOLocal(start),
        toISOLocal(end),
        granularity
      );

      setAnalyticsData(data);   
    } catch (err) {
      console.error('Failed to load analytics:', err);
      toast.error('Failed to load analytics data.');
    } finally {
      setAnalyticsLoading(false);
    }
  };

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

  const handleEditClick = () => {
    setEditForm({
      name: sector.name,
      description: sector.description || '',
      version: sector.version
    });
    setIsEditing(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!editForm.name.trim()) {
      toast.error("Sector name cannot be empty");
      return;
    }

    setIsSubmitting(true);
    try {
      const updatedSector = await updateSector(warehouseId, id, editForm);
      setSector(updatedSector);
      setIsEditing(false);
      toast.success("Sector updated successfully");
    } catch (err) {
      console.error("Failed to update sector", err);
      if (err.response && err.response.status === 409) {
          toast.error("Sector was modified by someone else. Please refresh.");
      } else {
          toast.error("Failed to update sector");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  };

  const getTempColor = (temp) => {
    if (temp === null || temp === undefined) return '#94a3b8';
    if (temp < 0)   return '#3b82f6';
    if (temp < 10)  return '#06b6d4';
    if (temp < 20)  return '#10b981';
    if (temp < 30)  return '#f59e0b';
    return '#ef4444';
  };

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

  if (loading) {
    return (
      <div className="sector-detail-container">
        <div className="loading-skeleton">
          <div className="skeleton-header" />
          <div className="skeleton-body" />
        </div>
      </div>
    );
  }

  if (error || !sector) {
    return (
      <div className="sector-detail-container">
        <div className="error-state">
          <h2>⚠️ Error Loading Sector</h2>
          <p>{error || 'Sector not found'}</p>
          <button className="btn-back" onClick={() => navigate(-1)}>Go Back</button>
        </div>
      </div>
    );
  }

  return (
    <div className="sector-detail-container">
      <ToastContainer position="top-right" autoClose={3000} />

      <div className="breadcrumb">
        <span onClick={() => navigate('/warehouses')} className="breadcrumb-link">
          Warehouses
        </span>
        <span className="breadcrumb-separator">/</span>
        <span
          onClick={() => navigate(`/warehouses/${sector.warehouseId}`)}
          className="breadcrumb-link"
        >
          {sector.warehouseName}
        </span>
        <span className="breadcrumb-separator">/</span>
        <span className="breadcrumb-current">{sector.name}</span>
      </div>

      <div className="sector-detail-header">
        <h1>{sector.name}</h1>
        <div className="header-actions">
          {!isEditing && (
            <button className="btn-edit" onClick={handleEditClick}>
              ✎ Edit Sector
            </button>
          )}
          <button
            className="btn-back"
            onClick={() => navigate(`/warehouses/${sector.warehouseId}`)}
          >
            ← Back to Warehouse
          </button>
        </div>
      </div>

      {isEditing ? (
        <div className="add-sector-form">
          <form onSubmit={handleEditSubmit}>
            <h3 className="edit-sector-title">Edit Sector Details</h3>
            <div className="form-group">
              <label htmlFor="edit-name">Sector Name *</label>
              <input
                id="edit-name"
                type="text"
                className="form-input"
                value={editForm.name}
                onChange={(e) => setEditForm({...editForm, name: e.target.value})}
                disabled={isSubmitting}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="edit-desc">Description</label>
              <textarea
                id="edit-desc"
                className="form-textarea"
                value={editForm.description}
                onChange={(e) => setEditForm({...editForm, description: e.target.value})}
                disabled={isSubmitting}
              />
            </div>
            
            <div className="form-actions-inline">
              <button
                type="button"
                className="btn-cancel-inline"
                onClick={() => setIsEditing(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn-submit-inline"
                disabled={isSubmitting}
              >
                {isSubmitting ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>
      ) : (
        <div className="sector-info-card">
          <div className="sector-info-grid">
            <div className="info-item">
              <span className="info-label">Warehouse</span>
              <span
                className="info-value info-link"
                onClick={() => navigate(`/warehouses/${sector.warehouseId}`)}
              >
                {sector.warehouseName}
              </span>
            </div>

            {sector.description && (
              <div className="info-item full-width">
                <span className="info-label">Description</span>
                <span className="info-value">{sector.description}</span>
              </div>
            )}

            <div className="info-item">
              <span className="info-label">Created</span>
              <span className="info-value">{formatDate(sector.createdAt)}</span>
            </div>

            <div className="info-item">
              <span className="info-label">Last Updated</span>
              <span className="info-value">{formatDate(sector.updatedAt)}</span>
            </div>
          </div>

          <div className="temperature-display">
            <div
              className="temp-circle"
              style={{ borderColor: getTempColor(sector.lastTemperature) }}
            >
              <span
                className="temp-value"
                style={{ color: getTempColor(sector.lastTemperature) }}
              >
                {sector.lastTemperature != null
                  ? `${sector.lastTemperature.toFixed(1)}°C`
                  : 'N/A'}
              </span>
              <span className="temp-label">Current Temp</span>
            </div>

            {sector.lastTemperatureReadingAt && (
              <p className="temp-reading-time">
                Last reading: {formatDate(sector.lastTemperatureReadingAt)}
              </p>
            )}
          </div>
        </div>
      )}

      <div className="sector-analytics-card">
        <h2>📈 Temperature Analytics</h2>

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
                      unit="°C" 
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: '#64748b', fontSize: 12 }}
                      width={45}
                      domain={['auto', 'auto']}
                    />
                    
                    <Tooltip 
                      labelFormatter={(label) => label} 
                      formatter={(val) => [`${val} °C`, 'Avg temp']}
                      contentStyle={{ 
                        borderRadius: '8px', 
                        border: 'none', 
                        boxShadow: '0 4px 15px rgba(0,0,0,0.1)' 
                      }}
                    />
                    
                    <Line 
                      type="monotone" 
                      dataKey="temperature" 
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

export default SectorDetailPage;
