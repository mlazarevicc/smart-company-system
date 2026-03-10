import React, { useState, useEffect, useRef } from 'react';
import websocketService from '../services/webSocketService';

function FactoryAvailabilityTest() {
  const [factoryId, setFactoryId] = useState(1);
  const [token, setToken] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [availabilityData, setAvailabilityData] = useState(null);
  const [messages, setMessages] = useState([]);
  
  // KORISTI useRef umesto useState za subscription
  const subscriptionRef = useRef(null);

  // Cleanup samo na unmount komponente
  useEffect(() => {
    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
      }
      websocketService.disconnect();
    };
  }, []); // PRAZNA dependency lista

  const addMessage = (msg) => {
    const timestamp = new Date().toLocaleTimeString();
    setMessages(prev => [...prev, { time: timestamp, text: msg }]);
  };

  const handleConnect = () => {
    if (!token) {
      alert('Please enter JWT token!');
      return;
    }

    addMessage('🔌 Connecting to WebSocket...');

    websocketService.connect(
      token,
      null,
      (error) => {
        addMessage(`❌ Connection error: ${error}`);
        setIsConnected(false);
      }
    );

    setTimeout(() => {
      setIsConnected(true);
      addMessage('✅ Connected successfully!');
    }, 1000);
  };

  const handleSubscribe = () => {
    if (!isConnected) {
      alert('Please connect first!');
      return;
    }

    addMessage(`📡 Subscribing to factory ${factoryId}...`);

    const sub = websocketService.subscribeToFactory(factoryId, (data) => {
      setAvailabilityData(data);
      addMessage(
        `📩 Update: Factory ${data.factoryName} - ` +
        `${data.currentStatus ? '🟢 Online' : '🔴 Offline'} - ` +
        `${data.percentageOnline}% uptime`
      );
    });

    subscriptionRef.current = sub; // Koristi ref umesto state
    addMessage(`✅ Subscribed to factory ${factoryId}`);
  };

  const handleUnsubscribe = () => {
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
      subscriptionRef.current = null;
      addMessage('📴 Unsubscribed');
    }
  };

  const handleDisconnect = () => {
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
      subscriptionRef.current = null;
    }
    websocketService.disconnect();
    setIsConnected(false);
    setAvailabilityData(null);
    addMessage('❌ Disconnected');
  };

  const handleRefresh = () => {
    websocketService.requestRefresh(factoryId);
    addMessage('🔄 Refresh requested');
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial' }}>
      <h1>🏭 Factory Availability Test</h1>

      {/* Configuration */}
      <div style={{ marginBottom: '20px', border: '1px solid #ccc', padding: '15px' }}>
        <h3>Configuration</h3>
        
        <div style={{ marginBottom: '10px' }}>
          <label>JWT Token: </label><br />
          <input
            type="text"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            placeholder="Paste your JWT token here"
            style={{ width: '500px', padding: '5px' }}
          />
        </div>

        <div style={{ marginBottom: '10px' }}>
          <label>Factory ID: </label>
          <input
            type="number"
            value={factoryId}
            onChange={(e) => setFactoryId(parseInt(e.target.value))}
            style={{ width: '100px', padding: '5px' }}
          />
        </div>

        <div>
          <button onClick={handleConnect} disabled={isConnected} style={buttonStyle}>
            Connect
          </button>
          <button onClick={handleSubscribe} disabled={!isConnected || subscriptionRef.current} style={buttonStyle}>
            Subscribe
          </button>
          <button onClick={handleUnsubscribe} disabled={!subscriptionRef.current} style={buttonStyle}>
            Unsubscribe
          </button>
          <button onClick={handleRefresh} disabled={!subscriptionRef.current} style={buttonStyle}>
            Refresh
          </button>
          <button onClick={handleDisconnect} disabled={!isConnected} style={buttonStyle}>
            Disconnect
          </button>
        </div>
      </div>

      {/* Status */}
      <div style={{ marginBottom: '20px', border: '1px solid #ccc', padding: '15px' }}>
        <h3>Status: 
          <span style={{ color: isConnected ? 'green' : 'red', marginLeft: '10px' }}>
            {isConnected ? '🟢 Connected' : '🔴 Disconnected'}
          </span>
        </h3>
        {subscriptionRef.current && <p>📡 Subscribed to factory {factoryId}</p>}
      </div>

      {/* Latest Data */}
      {availabilityData && (
        <div style={{ marginBottom: '20px', border: '1px solid #4CAF50', padding: '15px', backgroundColor: '#f0f8f0' }}>
          <h3>📊 Latest Availability Data</h3>
          <p><strong>Factory:</strong> {availabilityData.factoryName} (ID: {availabilityData.factoryId})</p>
          <p><strong>Status:</strong> {availabilityData.currentStatus ? '🟢 Online' : '🔴 Offline'}</p>
          <p><strong>Last Heartbeat:</strong> {availabilityData.lastHeartbeat || 'N/A'}</p>
          <p><strong>Online:</strong> {availabilityData.percentageOnline}% ({availabilityData.totalOnlineMinutes} min)</p>
          <p><strong>Offline:</strong> {availabilityData.percentageOffline}% ({availabilityData.totalOfflineMinutes} min)</p>
          <p><strong>Data Points:</strong> {availabilityData.dataPoints?.length || 0}</p>
          <p><strong>Timestamp:</strong> {availabilityData.timestamp}</p>
        </div>
      )}

      {/* Message Log */}
      <div style={{ border: '1px solid #ccc', padding: '15px' }}>
        <h3>Message Log</h3>
        <div style={{ 
          height: '300px', 
          overflowY: 'auto', 
          backgroundColor: '#f5f5f5', 
          padding: '10px',
          fontFamily: 'monospace',
          fontSize: '12px'
        }}>
          {messages.map((msg, index) => (
            <div key={index} style={{ marginBottom: '5px', borderLeft: '3px solid #2196F3', paddingLeft: '10px' }}>
              <strong>[{msg.time}]</strong> {msg.text}
            </div>
          ))}
        </div>
        <button 
          onClick={() => setMessages([])} 
          style={{ marginTop: '10px', padding: '5px 10px' }}
        >
          Clear Log
        </button>
      </div>
    </div>
  );
}

const buttonStyle = {
  padding: '8px 15px',
  margin: '5px',
  cursor: 'pointer',
  backgroundColor: '#2196F3',
  color: 'white',
  border: 'none',
  borderRadius: '4px'
};

export default FactoryAvailabilityTest;
