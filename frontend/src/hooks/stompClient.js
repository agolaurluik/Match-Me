import { Client } from '@stomp/stompjs';

export function createStompClient() {

  const storedToken = localStorage.getItem('Authorization');
  let token = storedToken ? storedToken.trim().replace(/^Bearer\s+/i, '') : null;

  if (!token) {
    throw new Error('Token required for STOMP client');
  }

  
  // console.log("Token being sent in WebSocket connection:", token);

  const brokerURL = `ws://localhost:8080/ws?token=${token}`;

  return new Client({
    brokerURL: brokerURL, 
    reconnectDelay: 5000,
    // debug: (str) => console.log('[STOMP]', str),
  });
}
