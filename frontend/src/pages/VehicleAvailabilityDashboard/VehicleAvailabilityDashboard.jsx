import React, { useState, useEffect, useRef } from 'react';
import { 
    AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, 
  ResponsiveContainer, PieChart, Pie, Cell, Legend
} from 'recharts';
import websocketService from '../../services/webSocketService';
import vehicleService from '../../services/vehicleService';
import './VehicleAvailabilityDashboard.css';

const COLORS = ['#10b981', '#ef4444']; // Green for Online, Red for Offline

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

const VehicleAvailabilityDashboard = ({ vehicleId }) => {
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
    let isActive = true; // Zastavica koja sprečava "zombi" ažuriranja
    let timeoutId = null; // Ref za čišćenje timeout-a

    const setupWebSocket = () => {
      const token = localStorage.getItem('token'); 
      if (!token) {
        if (isActive) setError('Authentication token missing. Please log in again.');
        return;
      }
      if (isActive) setLoading(true);

      websocketService.connect(
        token,
        null,
        (err) => {
          if (!isActive) return;
          console.error('WebSocket connection error:', err);
          setError('Failed to connect to real-time server.');
          setIsConnected(false);
          setLoading(false);
        }
      );

      timeoutId = setTimeout(() => {
        if (!isActive) return; // Ako je period promenjen pre isteka timeouta, odustani!
        setIsConnected(true);
                setLoading(false);
        
        subscriptionRef.current = websocketService.subscribeToVehicle(
          vehicleId, 
          (incomingData) => {
            if (!isActive) return; // Ignoriši ako smo prešli na istoriju

            // Koristimo funkciju unutar setData kako bismo imali pristup PRETHODNOM stanju (prevData)
            setData(prevData => {
              const parsedData = typeof incomingData === 'string' ? JSON.parse(incomingData) : incomingData;
              
              let updatedDataPoints = parsedData.dataPoints;

              // 1. SLUČAJ: Backend NIJE poslao celu listu u ovoj poruci (štedi protok)
              if (!updatedDataPoints || updatedDataPoints.length === 0) {
                // Preuzimamo staru listu sa grafika
                updatedDataPoints = prevData ? prevData.dataPoints : [];
                
                // Ako je backend poslao novu informaciju o trenutnom statusu, dodajemo je kao novu tačku
                if (parsedData.timestamp && parsedData.currentStatus !== undefined && updatedDataPoints.length > 0) {
                   const newPoint = {
                      timestamp: parsedData.timestamp,
                      isOnline: parsedData.currentStatus,
                      percentageOnline: parsedData.currentStatus ? 100 : 0, // Za "step" grafik
                      displayTime: new Date(parsedData.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                   };
                   
                   // Dodajemo novu tačku na kraj niza
                   updatedDataPoints = [...updatedDataPoints, newPoint];
                   
                   // Opciono: brišemo najstariju tačku kako grafik ne bi rastao u nedogled (čuvamo max ~180 tačaka za 3h)
                   if (updatedDataPoints.length > 180) {
                     updatedDataPoints.shift();
                   }
                }
              } 
              // 2. SLUČAJ: Backend JE poslao punu listu (npr. pri prvom povezivanju)
              else {
                 updatedDataPoints = updatedDataPoints.map(dp => {
                   const plotValue = dp.isOnline ? 100 : 0;
                   return {
                     ...dp,
                     percentageOnline: (dp.percentageOnline !== null && dp.percentageOnline !== undefined) 
                       ? dp.percentageOnline 
                       : plotValue,
                     displayTime: new Date(dp.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                   };
                 });
              }

              // Vraćamo spojen objekat
              return {
                ...parsedData,
                dataPoints: updatedDataPoints
              };
            });
          }
        );
      }, 1000);
    };

        const fetchHistoricalData = async () => {
      if (period === 'CUSTOM') {
        if (!fromDate || !toDate) return;
        
        const start = new Date(fromDate);
        const end = new Date(toDate);
        const diffDays = Math.ceil(Math.abs(end - start) / (1000 * 60 * 60 * 24)); 
        
        if (start > end) {
          if (isActive) setDateError('Početni datum mora biti pre krajnjeg datuma.');
          return;
        }
        if (diffDays > 365) {
          if (isActive) setDateError('Razlika ne sme biti veća od godinu dana.');
          return;
        }
        if (isActive) setDateError('');
      } else {
        if (isActive) setDateError('');
      }


          if (isActive) setLoading(true);
      try {
        const response = await vehicleService.getAvailabilityAnalytics(vehicleId, {
          period,
          fromDate: period === 'CUSTOM' ? fromDate : null,
          toDate: period === 'CUSTOM' ? toDate : null
        });

        if (!isActive) return; // Ako je prebačeno na drugi period dok se čekao API, odustani

        const formattedData = {
          ...response.data,
          dataPoints: response.data.dataPoints.map(dp => {
            const dateObj = new Date(dp.timestamp);
            const displayTime = ['LAST_1_HOUR', 'LAST_12_HOURS', 'LAST_24_HOURS'].includes(period) 
              ? dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : dateObj.toLocaleDateString([], { month: 'short', day: 'numeric' });
            return { ...dp, displayTime };
          })
        };

        setData(formattedData);
        setError('');
      } catch (err) {
        if (!isActive) return;
        console.error(err);
        setError('Failed to fetch historical availability data.');
      } finally {
        if (isActive) setLoading(false);
      }
    };

    // Logika za pozivanje prave funkcije na osnovu perioda
    if (period === 'LAST_3_HOURS') {
      setupWebSocket();
    } else {
      fetchHistoricalData();
    }

    // CLEANUP FUNKCIJA (Poziva se svaki put kad se promeni period)

    return () => {
      isActive = false; // Ubijamo zombi osluškivače
      if (timeoutId) clearTimeout(timeoutId); // Ubijamo timeout
      if (subscriptionRef.current) {
        try {
          if (typeof subscriptionRef.current.unsubscribe === 'function') {
            subscriptionRef.current.unsubscribe();
          }
        } catch (e) {
          console.error("Greška pri gašenju WebSocketa", e);
        }
        subscriptionRef.current = null;

      }
      setIsConnected(false);
    };
    }, [vehicleId, period, fromDate, toDate]);

  const isRealTime = period === 'LAST_3_HOURS';
  const pieData = data ? [
    { name: 'Online', value: data.percentageOnline || 0 },
    { name: 'Offline', value: data.percentageOffline || 0 }
  ] : [];


  return (
    <div className="availability-dashboard">
      <div className="availability-header">
        <div className="status-indicator">
          <span className={`status-dot ${isConnected ? 'connected' : 'disconnected'}`}></span>
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
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                  >
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
              <h4>{isRealTime ? 'Real-Time Status (Last 3h)' : 'Historical Availability'}</h4>
              {data.dataPoints?.length > 0 ? (
                <ResponsiveContainer width="100%" height={250}>
                  {isRealTime ? (
                    <AreaChart data={data.dataPoints}>
                      <defs>
                        <linearGradient id="colorUptime" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#10b981" stopOpacity={0.8}/>
                          <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                      <XAxis dataKey="displayTime" tick={{ fill: '#64748b', fontSize: 12 }} tickMargin={10} />
                      <YAxis domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                      <RechartsTooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}/>
                      <Area 
  type="stepAfter"
  dataKey="percentageOnline" 
  stroke="#10b981" 
  fillOpacity={1} 
  fill="url(#colorUptime)" 
  name="Status"
/>
                    </AreaChart>
                  ) : (
                    <BarChart data={data.dataPoints}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                      <XAxis dataKey="displayTime" tick={{ fill: '#64748b', fontSize: 12 }} tickMargin={10} />
                      <YAxis tick={{ fill: '#64748b', fontSize: 12 }} axisLine={false} tickLine={false} />
                      <RechartsTooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}/>
                      <Legend />
                      <Bar dataKey="onlineMinutes" name="Online (mins)" stackId="a" fill="#10b981" />
                      <Bar dataKey="offlineMinutes" name="Offline (mins)" stackId="a" fill="#ef4444" radius={[4, 4, 0, 0]} />
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

export default VehicleAvailabilityDashboard;