import { Client } from '@stomp/stompjs';
import SockJS from "sockjs-client/dist/sockjs"

class WebSocketService {
  constructor() {
    this.client = null;
    this.isConnected = false;
  }

  connect(token, onMessageCallback, onErrorCallback) {
    if (this.client && this.isConnected) {
      // console.log('Already connected');
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },

      debug: (str) => {
        // console.log('[STOMP Debug]', str);
      },

      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    
      reconnectDelay: 5000,

      onConnect: (frame) => { 
        // console.log('✅ WebSocket Connected', frame);
        this.isConnected = true;
      },

      onStompError: (frame) => {
        // console.error('❌ STOMP Error:', frame.headers['message']);
        // console.error('Details:', frame.body);
        if (onErrorCallback) {
          onErrorCallback(frame);
        }
      },

      onWebSocketError: (event) => {
        // console.error('❌ WebSocket Error:', event);
        if (onErrorCallback) {
          onErrorCallback(event);
        }
      },
      
      onWebSocketClose: (event) => {
        // console.warn('⚠️ WebSocket Closed:', event);
        this.isConnected = false;
      },

      onDisconnect: (frame) => { 
        // console.log('❌ WebSocket Disconnected', frame);
        this.isConnected = false;
      }
    });

    this.client.activate();
  }

  subscribeToFactory(factoryId, callback) {
    if (!this.client || !this.client.connected) {
      // console.error('WebSocket not connected');
      return null;
    }

    // console.log(`📡 Subscribing to factory ${factoryId}`);
    // console.log('📡 Client state:', {
    //   connected: this.client.connected,
    //   active: this.client.active
    // });

    const subscription = this.client.subscribe(
      `/topic/factory/${factoryId}/availability`,
      (message) => {
        try {
          const data = JSON.parse(message.body);
          // console.log('📩 Received availability update:', data);
          callback(data);
        } catch (error) {
          // console.error('Error parsing message:', error);
        }
      }
    );

    // console.log('Subscription created:', subscription.id);
    return subscription;
  }

  subscribeToWarehouse(warehouseId, callback) {
    if (!this.client || !this.client.connected) {
      // console.error('WebSocket not connected');
      return null;
    }

    // console.log(`📡 Subscribing to warehouse ${warehouseId}`);
    // console.log('📡 Client state:', {
    //   connected: this.client.connected,
    //   active: this.client.active
    // });

    const subscription = this.client.subscribe(
      `/topic/warehouse/${warehouseId}/temperature`,
      (message) => {
        try {
          const data = JSON.parse(message.body);
          // console.log('📩 Received temperature update:', data);
          callback(data);
        } catch (error) {
          // console.error('Error parsing message:', error);
        }
      }
    );

    // console.log('Subscription created:', subscription.id);
    return subscription;
  }

  subscribeToVehicle(vehicleId, callback) {
    if (!this.client || !this.client.connected) {
      // console.error('WebSocket not connected');
      return null;
    }

    // console.log(`📡 Subscribing to vehicle ${vehicleId}`);
    // console.log('📡 Client state:', {
    //   connected: this.client.connected,
    //   active: this.client.active
    // });

    const subscription = this.client.subscribe(
      `/topic/vehicle/${vehicleId}/availability`,
      (message) => {
        try {
          const data = JSON.parse(message.body);
          // console.log('📩 Received availability update:', data);
          callback(data);
        } catch (error) {
          // console.error('Error parsing message:', error);
        }
      }
    );

    // console.log('Subscription created:', subscription.id);
    return subscription;
  }

  requestRefresh(factoryId) {
    if (!this.client || !this.client.connected) {
      // console.error('Not connected to server');
      return;
    }

    this.client.publish({
      destination: `/app/factory/${factoryId}/availability/refresh`,
      body: JSON.stringify({})
    });
  }

  requestRefreshVehicle(vehicleId) {
    if (!this.client || !this.client.connected) {
      // console.error('Not connected to server');
      return;
    }

    this.client.publish({
      destination: `/app/vehicle/${vehicleId}/availability/refresh`,
      body: JSON.stringify({})
    });
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.isConnected = false;
    }
  }
}

export default new WebSocketService();
