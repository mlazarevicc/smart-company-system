import React, { useState, useEffect, useRef } from 'react';
import { 
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, 
  ResponsiveContainer, PieChart, Pie, Cell, Legend
} from 'recharts';
import websocketService from '../../services/webSocketService';
import factoryService from '../../services/factoryService';
import './FactoryAvailabilityDashboard.css';

const COLORS = ['#10b981', '#ef4444']; // Zelena za Online, Crvena za Offline

const AVAILABILITY_PERIODS = [
  { label: 'Last 1 Hour', value: 'LAST_1_HOUR' },
  { label: 'Last 3 Hours (Real-Time)', value: 'LAST_3_HOURS' },
  { label: 'Last 12 Hours', value: 'LAST_12_HOURS' },
  { label: 'Last 24 Hours', value: 'LAST_24_HOURS' },
  { label: 'Last Week', value: 'LAST_WEEK' },
  { label: 'Last Month', value: 'LAST_MONTH' },
  { label: 'Last 3 Months', value: 'LAST_3_MONTHS' },
  { label: 'Last Year', value: 'LAST_YEAR' },
  { label: 'Custom Range', value: 'CUSTOM' }
];

// Custom Tooltip za detaljan prikaz proponenata i minuta po specifikaciji
const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div style={{ backgroundColor: '#fff', padding: '12px', border: '1px solid #e2e8f0', borderRadius: '8px', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}>
        <p style={{ fontWeight: '600', margin: '0 0 8px 0', color: '#1e293b' }}>{label}</p>
        <p style={{ color: '#10b981', margin: '0 0 4px 0', fontSize: '14px' }}>
          <strong>Online:</strong> {data.percentageOnline?.toFixed(1)}% <span style={{color: '#64748b'}}>({data.onlineMinutes || 0} min)</span>
        </p>
        <p style={{ color: '#ef4444', margin: '0', fontSize: '14px' }}>
          <strong>Offline:</strong> {data.percentageOffline?.toFixed(1)}% <span style={{color: '#64748b'}}>({data.offlineMinutes || 0} min)</span>
        </p>
      </div>
    );
  }
  return null;
};

const FactoryAvailabilityDashboard = ({ factoryId }) => {
  const [period, setPeriod] = useState('LAST_3_HOURS'); 
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [dateError, setDateError] = useState('');
  
  const [data, setData] = useState(null); 
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [isConnected, setIsConnected] = useState(false);
  const subscriptionRef = useRef(null);

  useEffect(() => {
    let isActive = true;

    const setupWebSocket = () => {
      const token = localStorage.getItem('token'); 
      if (!token) {
        if (isActive) setError('Authentication token missing. Please log in again.');
        return;
      }

      websocketService.connect(
        token,
        null,
        (err) => {
          if (!isActive) return;
          console.error('WebSocket connection error:', err);
          setError('Failed to connect to real-time server.');
          setIsConnected(false);
        }
      );

      setIsConnected(true);
      
      subscriptionRef.current = websocketService.subscribeToFactory(
        factoryId, 
        (incomingData) => {
          if (!isActive) return;

          setData(prevData => {
            const parsedData = typeof incomingData === 'string' ? JSON.parse(incomingData) : incomingData;
            let currentPoints = prevData?.dataPoints ? [...prevData.dataPoints] : [];

            // 1. Priprema nove tačke
            let newPoint = null;
            if (parsedData.timestamp && parsedData.currentStatus !== undefined) {
               newPoint = {
                  timestamp: parsedData.timestamp,
                  isOnline: parsedData.currentStatus,
                  percentageOnline: parsedData.currentStatus ? 100 : 0,
                  percentageOffline: parsedData.currentStatus ? 0 : 100,
                  onlineMinutes: parsedData.currentStatus ? 1 : 0,
                  offlineMinutes: parsedData.currentStatus ? 0 : 1,
               };
            } else if (parsedData.dataPoints && parsedData.dataPoints.length > 0) {
               const last = parsedData.dataPoints[parsedData.dataPoints.length - 1];
               const plotVal = last.isOnline ? 100 : 0;
               newPoint = {
                   ...last,
                   percentageOnline: plotVal,
                   percentageOffline: 100 - plotVal
               };
            }

            // 2. Pametno spajanje (Deduplikacija po minutu)
            if (newPoint) {
                // Kreiramo string za trenutni minut (npr. "14:30")
                const newDisplayTime = new Date(newPoint.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                newPoint.displayTime = newDisplayTime;

                // Trazimo da li vec imamo podatak za ovaj minut u nizu
                const existingIndex = currentPoints.findIndex(p => p.displayTime === newDisplayTime);

                if (existingIndex !== -1) {
                    // Ako imamo, PREGAZI staro stanje za taj minut novim stanjem
                    // Ovo forsirano obaveštava Recharts da se vrednost promenila u okviru istog minuta
                    currentPoints[existingIndex] = newPoint;
                } else {
                    // Ako je počeo novi minut, dodaj novu tačku na kraj
                    currentPoints.push(newPoint);
                }
            }

            // 3. Obavezno sortiramo po vremenu
            currentPoints.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

            // 4. Ograničavamo na oko 180 tačaka (3 sata)
            if (currentPoints.length > 180) {
              currentPoints = currentPoints.slice(currentPoints.length - 180);
            }

            // KREIRAMO NOVI NIZ (zbog React re-rendera)
            return {
              ...prevData,
              ...parsedData,
              dataPoints: [...currentPoints] 
            };
          });
        }
      );
    };

    const fetchInitialData = async () => {
      // Validacija datuma
      if (period === 'CUSTOM') {
        if (!fromDate || !toDate) return;
        const start = new Date(fromDate);
        const end = new Date(toDate);
        const diffDays = Math.ceil(Math.abs(end - start) / (1000 * 60 * 60 * 24)); 
        
        if (start > end) { if (isActive) setDateError('Početni datum mora biti pre krajnjeg datuma.'); return; }
        if (diffDays > 365) { if (isActive) setDateError('Razlika ne sme biti veća od godinu dana.'); return; }
        if (isActive) setDateError('');
      } else {
        if (isActive) setDateError('');
      }

      if (isActive) setLoading(true);
      try {
        const response = await factoryService.getAvailabilityAnalytics(factoryId, {
          period, fromDate: period === 'CUSTOM' ? fromDate : null, toDate: period === 'CUSTOM' ? toDate : null
        });

        if (!isActive) return;

        // Priprema podataka pre prikazivanja
        const formattedDataPoints = response.data.dataPoints.map(dp => {
          const dateObj = new Date(dp.timestamp);
          const displayTime = ['LAST_1_HOUR', 'LAST_3_HOURS', 'LAST_12_HOURS', 'LAST_24_HOURS'].includes(period) 
            ? dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            : dateObj.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
          
          let pctOnline = dp.percentageOnline || 0;
          let pctOffline = 100 - pctOnline;

          // Za real-time prikaz grafik je tipa 'step', pa osiguravamo vrednosti 100 ili 0
          if (period === 'LAST_3_HOURS' && dp.isOnline !== undefined) {
             pctOnline = dp.isOnline ? 100 : 0;
             pctOffline = dp.isOnline ? 0 : 100;
          }

          return { ...dp, displayTime, percentageOnline: pctOnline, percentageOffline: pctOffline };
        }).sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp)); // Sortiranje istorije

        setData({ ...response.data, dataPoints: formattedDataPoints });
        setError('');

        // AKO JE REAL-TIME: Prvo smo popunili istoriju poslednja 3h, SADA palimo WebSocket za nove tačke
        if (period === 'LAST_3_HOURS') {
          setupWebSocket();
        }

      } catch (err) {
        if (!isActive) return;
        console.error(err);
        setError('Failed to fetch availability data.');
      } finally {
        if (isActive) setLoading(false);
      }
    };

    // UVEK prvo pozivamo istoriju API-jem (za sve periode, pa i za LAST_3_HOURS)
    fetchInitialData();

    return () => {
      isActive = false;
      if (subscriptionRef.current && typeof subscriptionRef.current.unsubscribe === 'function') {
        subscriptionRef.current.unsubscribe();
      }
      subscriptionRef.current = null;
      setIsConnected(false);
    };
  }, [factoryId, period, fromDate, toDate]);

  const isRealTime = period === 'LAST_3_HOURS';
  
  const pieData = data ? [
    { name: 'Online', value: data.percentageOnline || 0 },
    { name: 'Offline', value: data.percentageOffline || 0 }
  ] : [];

  return (
    <div className="availability-dashboard">
      <div className="availability-header">
        <div className="status-indicator">
          <h3>Availability Overview</h3>
          {isRealTime && (
            <>
              <span className={`status-dot ${isConnected ? 'connected' : 'disconnected'}`}></span>
              <span className="status-text">{isConnected ? 'Live Stream Active' : 'Connecting...'}</span>
            </>
          )}
        </div>
        
        <div className="fad-controls">
          <select className="fad-select" value={period} onChange={(e) => setPeriod(e.target.value)}>
            {AVAILABILITY_PERIODS.map(p => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
          {period === 'CUSTOM' && (
            <div className="fad-custom-date-group">
              <input type="date" className="fad-date-input" value={fromDate} max={toDate || undefined} onChange={e => setFromDate(e.target.value)} />
              <span>-</span>
              <input type="date" className="fad-date-input" value={toDate} min={fromDate || undefined} onChange={e => setToDate(e.target.value)} />
            </div>
          )}
        </div>
      </div>

      {error && <div className="fad-error-msg">{error}</div>}
      {dateError && <div className="fad-error-msg">{dateError}</div>}

      {loading && !data ? (
        <div className="fad-loading-state">Loading data...</div>
      ) : data ? (
        <>
          <div className="metrics-grid">
            <div className="stat-card">
              <span className="stat-label">Online Time</span>
              <span className="stat-value text-green">{data.percentageOnline?.toFixed(1)}%</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Offline Time</span>
              <span className="stat-value text-red">{data.percentageOffline?.toFixed(1)}%</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Total Uptime</span>
              <span className="stat-value text-small">{data.totalOnlineMinutes} mins</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Total Downtime</span>
              <span className="stat-value text-small">{data.totalOfflineMinutes} mins</span>
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
              {data.dataPoints?.length > 0 ? (
                <ResponsiveContainer width="100%" height={250}>
                  {isRealTime ? (
                    <AreaChart data={data.dataPoints}>
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
                      <YAxis domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 12 }} tickFormatter={(val) => `${val}%`} axisLine={false} tickLine={false} />
                      <RechartsTooltip content={<CustomTooltip />} />
                      <Area 
                        type="stepAfter"
                        dataKey="percentageOnline" 
                        stroke="#10b981"
                        strokeWidth={2}
                        fillOpacity={1} 
                        fill="url(#colorUptime)" 
                        name="Online %"
                      />
                    </AreaChart>
                  ) : (
                    <BarChart data={data.dataPoints} barSize={30}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                      <XAxis 
                        dataKey="timestamp" 
                        tickFormatter={(timeStr) => {
                          const d = new Date(timeStr);
                          return ['LAST_1_HOUR', 'LAST_3_HOURS', 'LAST_12_HOURS', 'LAST_24_HOURS'].includes(period) 
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

export default FactoryAvailabilityDashboard;