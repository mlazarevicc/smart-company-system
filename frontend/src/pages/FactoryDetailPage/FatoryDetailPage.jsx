// src/pages/factory/FactoryDetailPage.jsx
import React, { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import factoryService from '../../services/factoryService';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import './FactoryDetailPage.css';
import FactoryAvailabilityDashboard from './FactoryAvailabilityDashboard';
import SecureImage from '../../components/SecureImage';

const TIME_PERIODS = [
  { label: 'Last Week', value: 'LAST_WEEK' },
  { label: 'Last Month', value: 'LAST_MONTH' },
  { label: 'Last 3 Months', value: 'LAST_3_MONTHS' },
  { label: 'Last 6 Months', value: 'LAST_6_MONTHS' },
  { label: 'Last Year', value: 'LAST_YEAR' },
  { label: 'Custom Range', value: 'CUSTOM' }
];

const FactoryDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [factory, setFactory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [selectedProductId, setSelectedProductId] = useState(null);
  const [period, setPeriod] = useState('LAST_WEEK');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  
  const [analyticsData, setAnalyticsData] = useState([]);
  const [totalProduced, setTotalProduced] = useState(null); // Dodato za ukupnu proizvodnju
  const [dateError, setDateError] = useState(''); // Dodato za validaciju datuma
  const [loadingAnalytics, setLoadingAnalytics] = useState(false);

  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const productsPerPage = 10;

  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const nextImage = () => {
    if (!factory?.imageUrls) return;
    setCurrentImageIndex((prev) => (prev + 1) % factory.imageUrls.length);
  };

  const prevImage = () => {
    if (!factory?.imageUrls) return;
    setCurrentImageIndex((prev) => 
      prev === 0 ? factory.imageUrls.length - 1 : prev - 1
    );
  };

  useEffect(() => {
    loadFactory();
  }, [id]);

  useEffect(() => {
    if (selectedProductId) fetchAnalytics();
  }, [selectedProductId, period, fromDate, toDate]);

  const loadFactory = async () => {
    setLoading(true);
    try {
      const response = await factoryService.getFactoryById(id);
      setFactory(response.data);
      if (response.data.products?.length > 0) {
        setSelectedProductId(response.data.products[0].id);
      }
    } catch (err) {
      setError('Failed to load factory details.');
    } finally {
      setLoading(false);
    }
  };

  const fetchAnalytics = async () => {
    // Validacija Custom datuma
    if (period === 'CUSTOM') {
      if (!fromDate || !toDate) {
        setTotalProduced(null);
        setAnalyticsData([]);
        return;
      }
      
      const start = new Date(fromDate);
      const end = new Date(toDate);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
      
      if (start > end) {
        setDateError('Početni datum mora biti pre krajnjeg datuma.');
        return;
      }
      if (diffDays > 365) {
        setDateError('Razlika između datuma ne sme biti veća od godinu dana.');
        return;
      }
      setDateError('');
    } else {
      setDateError('');
    }

    setLoadingAnalytics(true);
    try {
      const response = await factoryService.getProductionAnalytics(id, {
        productId: selectedProductId,
        period,
        fromDate: period === 'CUSTOM' ? fromDate : null,
        toDate: period === 'CUSTOM' ? toDate : null
      });
      
      setTotalProduced(response.data.totalProduced);
      
      setAnalyticsData(response.data.dataPoints.map(dp => {
        const dateObj = new Date(dp.timestamp);
        // Lepše formatiranje datuma zavisno od toga da li ima sate/minute
        const displayDate = response.data.granularity === 'HOURLY' || response.data.granularity === 'DAILY'
          ? dateObj.toLocaleDateString()
          : dateObj.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
          
        return {
          ...dp,
          displayDate
        };
      }));
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingAnalytics(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this factory?')) return;
    try {
      await factoryService.deleteFactory(id);
      navigate('/factories');
    } catch (err) {
      alert('Delete failed');
    }
  };

  const filteredProducts = factory?.products?.filter(p => 
    p.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
    p.sku.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  const totalPages = Math.ceil(filteredProducts.length / productsPerPage);
  const currentProducts = filteredProducts.slice((currentPage - 1) * productsPerPage, currentPage * productsPerPage);

  if (loading) return <div className="fdp-container"><div className="fdp-loading">Loading...</div></div>;
  if (error || !factory) return <div className="fdp-container"><div className="fdp-error">{error}</div></div>;

  return (
<div className="fdp-container">
  <div className="fdp-header">
    <div className="fdp-title-section">
      <h1>{factory.name}</h1>
      <span className={`fdp-status-badge ${factory.isOnline ? 'fdp-online' : 'fdp-offline'}`}>
        {factory.isOnline ? 'ONLINE' : 'OFFLINE'}
      </span>
    </div>
    <div className="fdp-header-actions">
      <button className="fdp-btn fdp-btn-back" onClick={() => navigate('/factories')}>View list</button>
      <button className="fdp-btn fdp-btn-edit" onClick={() => navigate(`/factories/${id}/edit`)}>Edit</button>
      <button className="fdp-btn fdp-btn-delete" onClick={handleDelete}>Delete</button>
    </div>
  </div>

  <div className="fdp-dashboard">
    {/* 1. GORE LEVO: General Information */}
    <div className="fdp-card">
      <h2 className="fdp-card-title">General Information</h2>
      <div className="fdp-info-grid">
        <div className="fdp-info-group"><label>Address</label><p>{factory.address}</p></div>
        <div className="fdp-info-group"><label>Location</label><p>{factory.city}, {factory.country}</p></div>
        <div className="fdp-info-group"><label>Coordinates</label><p>{factory.latitude?.toFixed(4)}, {factory.longitude?.toFixed(4)}</p></div>
        {/* <div className="fdp-info-group"><label>Last Heartbeat</label><p>{factory.lastHeartbeat ? new Date(factory.lastHeartbeat).toLocaleString() : 'N/A'}</p></div> */}
      </div>
    </div>

    {/* 2. GORE DESNO: Analytics */}
    <div className="fdp-card">
      <div className="fdp-analytics-header">
        <h2 className="fdp-card-title">Production Analytics</h2>
        <div className="fdp-controls-wrapper">
          <div className="fdp-controls">
            <select className="fdp-select" value={period} onChange={(e) => setPeriod(e.target.value)}>
              {TIME_PERIODS?.map(p => <option key={p.value} value={p.value}>{p.label}</option>)}
            </select>
            {period === 'CUSTOM' && (
              <div className="fdp-custom-date-group">
                <input type="date" className="fdp-date-input" value={fromDate} max={toDate || undefined} onChange={e => setFromDate(e.target.value)} />
                <span className="fdp-date-separator">-</span>
                <input type="date" className="fdp-date-input" value={toDate} min={fromDate || undefined} onChange={e => setToDate(e.target.value)} />
              </div>
            )}
          </div>
        </div>
      </div>
      
      {/* Prikaz greške ili metrike za ukupnu proizvodnju */}
      <div className="fdp-analytics-summary">
        {dateError ? (
          <div className="fdp-error-text">{dateError}</div>
        ) : (
          totalProduced !== null && !loadingAnalytics && (
            <div className="fdp-total-produced">
              Total produced: <span className="fdp-highlight-value">{totalProduced}</span> unit
            </div>
          )
        )}
      </div>

      <div className="fdp-chart-box">
        {loadingAnalytics ? <div className="fdp-chart-info">Loading chart...</div> : 
          analyticsData?.length > 0 && !dateError ? (
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={analyticsData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
              <XAxis dataKey="displayDate" tick={{fontSize: 11, fill: '#64748b'}} tickMargin={10} />
              <YAxis tick={{fontSize: 11, fill: '#64748b'}} axisLine={false} tickLine={false} />
              <Tooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
              <Line type="monotone" dataKey="quantity" name="Produced" stroke="#0ea5e9" strokeWidth={3} dot={{r:4, fill: '#0ea5e9', strokeWidth: 0}} activeDot={{r:6}} />
            </LineChart>
          </ResponsiveContainer>
        ) : <div className="fdp-placeholder">No data for selected period.</div>}
      </div>
    </div>

    {/* 3. DOLE LEVO: Images */}
    <div className="fdp-card">
      <h2 className="fdp-card-title">Images</h2>
      <div className="fdp-image-section">
        {factory.imageUrls?.length > 0 ? (
          <div className="fdp-image-container">
            <SecureImage 
              imageUrl={factory.imageUrls[currentImageIndex]} 
              altText={`${factory.name} - Image ${currentImageIndex + 1}`} 
              className="fdp-detail-image" 
            />
            {factory.imageUrls.length > 1 && (
              <>
                <button className="fdp-image-nav-btn fdp-prev" onClick={prevImage}>‹</button>
                <button className="fdp-image-nav-btn fdp-next" onClick={nextImage}>›</button>
                <div className="fdp-image-counter">
                  {currentImageIndex + 1} / {factory.imageUrls.length}
                </div>
              </>
            )}
          </div>
        ) : (
          <p className="fdp-placeholder">No images available.</p>
        )}
      </div>
    </div>

    {/* 4. DOLE DESNO: Products Table */}
    <div className="fdp-card">
      <div className="fdp-table-header">
        <h2 className="fdp-card-title">Products ({filteredProducts?.length || 0})</h2>
        <input 
          className="fdp-search-input"
          placeholder="Search products..." 
          value={searchTerm}
          onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(1); }}
        />
      </div>
      <div className="fdp-table-scroll">
        <table className="fdp-table">
          <thead>
            <tr>
              <th>SKU</th>
              <th>Name</th>
              <th>Category</th>
              <th>Price</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {currentProducts?.map(p => (
              <tr key={p.id} className={selectedProductId === p.id ? 'fdp-row-selected' : ''} onClick={() => setSelectedProductId(p.id)}>
                <td><code>{p.sku}</code></td>
                <td><strong>{p.name}</strong></td>
                <td>{p.category_name}</td>
                <td>{p.display_price}</td>
                <td>
                  <span className={`fdp-pill ${p.is_available ? 'fdp-pill-active' : 'fdp-pill-inactive'}`}>
                    {p.is_available ? 'Available' : 'Out of Stock'}
                  </span>
                </td>
                <td><Link to={`/products/${p.id}`} className="fdp-link-btn">View</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <div className="fdp-pagination">
          <button disabled={currentPage === 1} onClick={() => setCurrentPage(c => c - 1)}>Previous</button>
          <span className="fdp-page-info">Page {currentPage} of {totalPages}</span>
          <button disabled={currentPage === totalPages} onClick={() => setCurrentPage(c => c + 1)}>Next</button>
        </div>
      )}
    </div>
  </div>
  
  <FactoryAvailabilityDashboard factoryId={id} />
</div>
  );
};

export default FactoryDetailPage;