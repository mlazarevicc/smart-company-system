import React, { useState, useEffect } from 'react';
import { 
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend
} from 'recharts';
import { getAvailabilityAnalytics } from '../../services/warehouseService';
import { toast } from 'react-toastify';
import './WarehouseAvailabilityCharts.css';

const PERIODS = [
  { label: 'Last 1 Hour', value: '1h', hours: 1, granularity: '1m' },
  { label: 'Last 3 Hours (Live)', value: '3h', hours: 3, granularity: '5m' },
  { label: 'Last 12 Hours', value: '12h', hours: 12, granularity: '15m' },
  { label: 'Last 24 Hours', value: '24h', hours: 24, granularity: '1h' },
  { label: 'Last Week', value: '1w', hours: 24 * 7, granularity: '4h' },
  { label: 'Last Month', value: '1m', hours: 24 * 30, granularity: '1d' },
  { label: 'Last 3 Months', value: '3m', hours: 24 * 90, granularity: '1d' },
  { label: 'Last Year', value: '1y', hours: 24 * 365, granularity: '1w' },
  { label: 'Custom Range', value: 'custom' }
];
const COLORS = ['#10b981', '#ef4444'];

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div style={{ backgroundColor: '#fff', padding: '12px', border: '1px solid #e2e8f0', borderRadius: '8px', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}>
        <p style={{ fontWeight: '600', margin: '0 0 8px 0', color: '#1e293b' }}>{label}</p>
        <p style={{ color: '#10b981', margin: '0 0 4px 0', fontSize: '14px' }}>
          <strong>Online:</strong> {data.numericValue !== undefined ? (data.numericValue * 100).toFixed(1) : (data.percentageOnline?.toFixed(1))}% <span style={{color: '#64748b'}}>({data.onlineMinutes || data.totalOnlineMinutes || 0} min)</span>
        </p>
        <p style={{ color: '#ef4444', margin: '0', fontSize: '14px' }}>
          <strong>Offline:</strong> {data.numericValue !== undefined ? (100 - data.numericValue * 100).toFixed(1) : (data.percentageOffline?.toFixed(1))}% <span style={{color: '#64748b'}}>({data.offlineMinutes || data.totalOfflineMinutes || 0} min)</span>
        </p>
      </div>
    );
  }
  return null;
};

const WarehouseAvailabilityCharts = ({ warehouseId, realtimeData }) => {
  const [period, setPeriod] = useState('3h');
  const [customDates, setCustomDates] = useState({ start: '', end: '' });
  const [analyticsData, setAnalyticsData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [dateError, setDateError] = useState('');

   useEffect(() => {
    if (period === '3h' && realtimeData) {
      setAnalyticsData(prev => {
        if (!prev || !prev.dataPoints) return prev;

        const cleanTimestamp = realtimeData.timestamp.endsWith('Z') 
                             ? realtimeData.timestamp.slice(0, -1) 
                             : realtimeData.timestamp;
        
        const dateObj = new Date(cleanTimestamp);
        
        const isOnlineNow = realtimeData.isOnline ?? prev.dataPoints[prev.dataPoints.length - 1]?.isOnline ?? false;

        const newPoint = {
          timestamp: cleanTimestamp,
          isOnline: isOnlineNow,
          numericValue: isOnlineNow ? 1 : 0,
          formattedTime: dateObj.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }),
          rawDate: dateObj,
          percentageOnline: isOnlineNow ? 100 : 0,
          percentageOffline: isOnlineNow ? 0 : 100
        };

        const threeHoursAgo = new Date();
        threeHoursAgo.setHours(threeHoursAgo.getHours() - 3);

        const updatedPoints = [...prev.dataPoints, newPoint]
          .filter(dp => dp.rawDate >= threeHoursAgo) // Zadrži samo poslednja 3 sata
          .sort((a, b) => a.rawDate.getTime() - b.rawDate.getTime());

        let newOnlineMins = prev.totalOnlineMinutes || 0;
        let newOfflineMins = prev.totalOfflineMinutes || 0;
        
        if (isOnlineNow) {
          newOnlineMins += 1; // Ako dobijamo poruku na minut
        } else {
          newOfflineMins += 1;
        }

        const totalMins = newOnlineMins + newOfflineMins;
        const newPercentageOnline = totalMins > 0 ? (newOnlineMins / totalMins) * 100 : 0;
        const newPercentageOffline = totalMins > 0 ? (newOfflineMins / totalMins) * 100 : 0;

        return { 
          ...prev, 
          dataPoints: updatedPoints,
          totalOnlineMinutes: newOnlineMins,
          totalOfflineMinutes: newOfflineMins,
          percentageOnline: newPercentageOnline,
          percentageOffline: newPercentageOffline
        };
      });
    }
  }, [realtimeData, period]);

  useEffect(() => {
    if (period !== 'custom') {
      fetchData(PERIODS.find(p => p.value === period));
    }
  }, [period, warehouseId]);

  const fetchData = async (range, customStart = null, customEnd = null) => {
    setLoading(true);
    setError('');
    setDateError('');
    try {
      let end, start;
      if (range.value === 'custom') {
        end = new Date(customEnd);
        start = new Date(customStart);
        const diffDays = Math.ceil(Math.abs(end - start) / (1000 * 60 * 60 * 24));
        if (start > end) { setDateError('Start date must be before end date.'); setLoading(false); return; }
        if (diffDays > 365) { setDateError('Date range cannot exceed 1 year.'); setLoading(false); return; }
      } else {
        end = new Date();
        start = new Date(end.getTime() - range.hours * 60 * 60 * 1000);
      }
      const data = await getAvailabilityAnalytics(warehouseId, start, end, range.granularity);
      const isLongPeriod = range.hours > 24 || range.value === 'custom';
      const mappedPoints = data.dataPoints.map(dp => {
        const cleanTimestamp = dp.timestamp.replace('Z', '');
        const dateObj = new Date(cleanTimestamp);
        let formattedTime;
        if (isLongPeriod) {
          formattedTime = dateObj.toLocaleString('en-GB', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
        } else {
          formattedTime = dateObj.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
        }
        return {
          ...dp,
          numericValue: dp.uptimePercentage !== undefined ? dp.uptimePercentage / 100 : (dp.isOnline ? 1 : 0),
          formattedTime,
          rawDate: dateObj,
          percentageOnline: dp.uptimePercentage !== undefined ? dp.uptimePercentage : (dp.isOnline ? 100 : 0),
          percentageOffline: dp.uptimePercentage !== undefined ? 100 - dp.uptimePercentage : (dp.isOnline ? 0 : 100)
        };
      });
      mappedPoints.sort((a, b) => a.rawDate - b.rawDate);
      setAnalyticsData({ ...data, dataPoints: mappedPoints });
    } catch (error) {
      setError('Failed to load availability data');
      toast.error('Failed to load availability data');
    } finally {
      setLoading(false);
    }
  };


  const handleCustomSubmit = (e) => {
    e.preventDefault();
    fetchData(PERIODS.find(p => p.value === 'custom'), customDates.start, customDates.end);
  };

  // Pie chart data
  const pieData = analyticsData ? [
    { name: 'Online', value: analyticsData.percentageOnline || 0 },
    { name: 'Offline', value: analyticsData.percentageOffline || 0 }
  ] : [];

  // Real-time period check
  const isRealTime = period === '3h';

  return (
    <div className="availability-dashboard">
      <div className="availability-header">
        <div className="status-indicator">
          <h3>Warehouse Availability Overview</h3>
          {isRealTime && (
            <>
              <span className="status-dot connected"></span>
              <span className="status-text">Live Stream Active</span>
            </>
          )}
        </div>
        <div className="fad-controls">
          <select className="fad-select" value={period} onChange={e => setPeriod(e.target.value)}>
            {PERIODS.map(p => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
          {period === 'custom' && (
            <div className="fad-custom-date-group">
              <input type="datetime-local" className="fad-date-input" value={customDates.start} max={customDates.end || undefined} onChange={e => setCustomDates({...customDates, start: e.target.value})} />
              <span>-</span>
              <input type="datetime-local" className="fad-date-input" value={customDates.end} min={customDates.start || undefined} onChange={e => setCustomDates({...customDates, end: e.target.value})} />
              <button type="button" className="btn-submit-inline" onClick={handleCustomSubmit}>Apply</button>
            </div>
          )}
        </div>
      </div>

      {error && <div className="fad-error-msg">{error}</div>}
      {dateError && <div className="fad-error-msg">{dateError}</div>}

      {loading && !analyticsData ? (
        <div className="fad-loading-state">Loading data...</div>
      ) : analyticsData ? (
        <>
          <div className="metrics-grid">
            <div className="stat-card">
              <span className="stat-label">Online Time</span>
              <span className="stat-value text-green">{analyticsData.percentageOnline?.toFixed(1)}%</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Offline Time</span>
              <span className="stat-value text-red">{analyticsData.percentageOffline?.toFixed(1)}%</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Total Uptime</span>
              <span className="stat-value text-small">{analyticsData.totalOnlineMinutes} mins</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Total Downtime</span>
              <span className="stat-value text-small">{analyticsData.totalOfflineMinutes} mins</span>
            </div>
          </div>

          <div className="charts-container">
            <div className="chart-box">
              <h4>Overall Availability</h4>
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={80} paddingAngle={5} dataKey="value">
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <RechartsTooltip formatter={(value) => `${value.toFixed(1)}%`} />
                  <Legend verticalAlign="bottom" height={36}/>
                </PieChart>
              </ResponsiveContainer>
            </div>

            <div className="chart-box">
              <h4>{isRealTime ? 'Real-Time Status (Last 3h)' : 'Historical Availability Timeline'}</h4>
              {analyticsData.dataPoints?.length > 0 ? (
                <ResponsiveContainer width="100%" height={250}>
                  {isRealTime ? (
                    <AreaChart data={analyticsData.dataPoints}>
                      <defs>
                        <linearGradient id="colorUptime" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#10b981" stopOpacity={0.6}/>
                          <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                      <XAxis 
                        dataKey="timestamp" 
                        tickFormatter={(timeStr) => new Date(timeStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        tick={{ fill: '#64748b', fontSize: 12 }} 
                        tickMargin={10} 
                        minTickGap={20} 
                      />
                      <YAxis domain={[0, 1]} tick={{ fill: '#64748b', fontSize: 12 }} tickFormatter={(val) => `${(val * 100).toFixed(0)}%`} axisLine={false} tickLine={false} />
                      <RechartsTooltip content={<CustomTooltip />} />
                      <Area 
                        type="stepAfter"
                        dataKey="numericValue" 
                        stroke="#10b981"
                        strokeWidth={2}
                        fillOpacity={1} 
                        fill="url(#colorUptime)" 
                        name="Online %"
                      />
                    </AreaChart>
                  ) : (
                    <BarChart data={analyticsData.dataPoints} barSize={30}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                      <XAxis 
                        dataKey="timestamp" 
                        tickFormatter={(timeStr) => {
                          const d = new Date(timeStr);
                          return ['1h', '3h', '12h', '24h'].includes(period) 
                            ? d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                            : d.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
                        }}
                        tick={{ fill: '#64748b', fontSize: 12 }} 
                        tickMargin={10} 
                        minTickGap={20} 
                      />
                      <YAxis domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 12 }} tickFormatter={(val) => `${val}%`} axisLine={false} tickLine={false} />
                      <RechartsTooltip content={<CustomTooltip />} cursor={{fill: 'rgba(0,0,0,0.04)'}} />
                      <Legend iconType="circle" />
                      <Bar dataKey="percentageOnline" name="Online" stackId="a" fill="#10b981" radius={[0, 0, 4, 4]} />
                      <Bar dataKey="percentageOffline" name="Offline" stackId="a" fill="#ef4444" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  )}
                </ResponsiveContainer>
              ) : (
                <div className="no-data-msg">No data points available for this period.</div>
              )}
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
};

export default WarehouseAvailabilityCharts;
