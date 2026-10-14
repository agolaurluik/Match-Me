import React, { createContext, useContext, useEffect, useState, useRef } from 'react';
import { createStompClient } from '../../../socket/stompClient';

export const StompContext = createContext();

export const StompProvider = ({ children }) => {
  const clientRef = useRef(null);
  const [refreshConnectionStatusFn, setRefreshConnectionStatusFn] = useState(null);
  const [userId, setUserId] = useState(null);
  const [token, setToken] = useState(null);
  const [messages, setMessages] = useState([]);
  const [onlineUsers, setOnlineUsers] = useState({});
  const [typingStatus, setTypingStatus] = useState({});
  const typingTimeouts = useRef({});
  const [isConnected, setIsConnected] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [hasUnseenIncomingConnections, setHasUnseenIncomingConnections] =
    useState(false);

  useEffect(() => {
    const storedToken = localStorage.getItem('Authorization');
    if (!storedToken) {
      console.warn('No token found in localStorage — STOMP client will not connect');
      return;
    }

    const token = storedToken.trim().replace(/^Bearer\s+/i, '');
    setToken(token);
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (payload && payload.id) {
        setUserId(payload.id);
      } else {
        console.error('No user id found in the token payload');
      }
    } catch (error) {
      console.error('Failed to decode or parse token:', error);
    }
  }, []);

  useEffect(() => {
    if (!token) return;

    clientRef.current = createStompClient(token);
    const client = clientRef.current;

    client.onConnect = () => {
      // console.log('[STOMP] Connected!');
      setIsConnected(true);

      client.subscribe('/user/queue/messages', (msg) => {
        const body = JSON.parse(msg.body);
        setMessages((prev) => [...prev, body]);
      });

      client.subscribe('/user/queue/read-receipts', (msg) => {
        const messageId = JSON.parse(msg.body);

        setMessages(prev =>
          prev.map(m =>
            Number(m.messageId) === Number(messageId)
              ? { ...m, status: 'READ' }
              : m
          )
        );
      });

      client.subscribe('/topic/onlineUsers', (msg) => {
        const onlineUsersList = JSON.parse(msg.body);
        setOnlineUsers(onlineUsersList);
      });

      client.subscribe('/user/queue/onlineStatusResponse', (msg) => {
        const onlineStatusMap = JSON.parse(msg.body);
        setOnlineUsers((prevOnlineUsers) => ({
          ...prevOnlineUsers,
          ...onlineStatusMap
        }));
      });

      client.subscribe('/user/queue/typing', (msg) => {
        const payload = JSON.parse(msg.body);
        if (payload.senderId !== undefined && payload.typing !== undefined) {
          setTypingStatus((prev) => ({
            ...prev,
            [payload.senderId]: payload.typing,
          }));
        }

        if (typingTimeouts.current[payload.senderId]) {
          clearTimeout(typingTimeouts.current[payload.senderId]);
        }

        if (payload.typing) {
          typingTimeouts.current[payload.senderId] = setTimeout(() => {
            setTypingStatus((prev) => ({
              ...prev,
              [payload.senderId]: false,
            }));
          }, 2000);
        }
      });

      client.subscribe(`/user/queue/connection-status-update`, (message) => {
        const payload = JSON.parse(message.body);
        const otherUserId = payload.userId;
        // console.log("Received update signal from userID: " + otherUserId);
        refreshConnectionStatusFn?.(otherUserId);
      });

      client.subscribe('/user/queue/errors', (frame) => {
        console.log('[STOMP] Received error:', frame.body);

        try {
          const error = JSON.parse(frame.body);
          console.log('[STOMP] Parsed error:', error);

          setErrorMessage(error.message || 'Something went wrong.');
        } catch (err) {
          console.error('[STOMP] Failed to parse error:', err);
          setErrorMessage('An unexpected chat error occurred.');
        }
      });


      client.subscribe('/user/queue/receiverHasUnseenNotifications', (frame) => {
        try {
          const hasUnseen = JSON.parse(frame.body);

          console.log('[STOMP] Unseen incoming notifications:', hasUnseen);

          setHasUnseenIncomingConnections(Boolean(hasUnseen));
        } catch (error) {
          console.error('Failed to parse unseen notification status:', error);
        }
      });

    };

    client.onWebSocketError = (error) => {
      console.error('[STOMP] WebSocket error:', error);
    };

    client.onStompError = (frame) => {
      const message =
        frame.headers?.message || 'A connection error occurred.';

      console.error('[STOMP] Broker reported error:', message);
      setErrorMessage(message);
    };

    client.onWebSocketClose = () => {
      setIsConnected(false);
      console.warn('[STOMP] WebSocket closed');
    };

    client.onDisconnect = () => {
      setIsConnected(false);
      // console.log('[STOMP] Disconnected');
    };

    client.activate();

    return () => {
      client.deactivate();
      setIsConnected(false);
      Object.values(typingTimeouts.current).forEach(clearTimeout);
    };
  }, [token]);


  /*
  useEffect(() => {
    if (isConnected && userId && connections.length > 0) {
      sendOnlineStatusRequest(userId, connections);
    }
  }, [isConnected, userId, connections]);
  */

  const sendMessage = (senderId, receiverId, content) => {
    const payload = JSON.stringify({ senderId, receiverId, content });
    const client = clientRef.current;

    if (client && client.connected) {
      client.publish({
        destination: '/app/messages',
        headers: {},
        body: payload,
      });
    } else {
      console.warn('STOMP client not connected - cannot send message');
    }
  };

  const sendTypingStatus = (senderId, receiverId, isTyping) => {
    const payload = JSON.stringify({ senderId, receiverId, typing: isTyping });
    const client = clientRef.current;

    if (client && client.connected) {
      client.publish({
        destination: '/app/typing',
        headers: {},
        body: payload,
      });
    } else {
      console.warn('STOMP client not connected - cannot send typing status');
    }

    if (isTyping) {
      if (typingTimeouts.current[senderId]) {
        clearTimeout(typingTimeouts.current[senderId]);
      }

      typingTimeouts.current[senderId] = setTimeout(() => {
        sendTypingStatus(senderId, receiverId, false);
      }, 2000);
    } else {
      if (typingTimeouts.current[senderId]) {
        clearTimeout(typingTimeouts.current[senderId]);
      }
    }
  };

  const clearErrorMessage = () => {
    setErrorMessage('');
  };

  const sendOnlineStatusRequest = (userId, connections) => {
    const client = clientRef.current;
    const friendIds = connections;

    if (!isConnected) {
      console.warn('STOMP client not connected - cannot request online status');
      return;
    }

    if (friendIds.length === 0) {
      console.warn("No valid friendIds to send.");
      return;
    }

    const payload = { userId, friendIds };

    client.publish({
      destination: '/app/onlineStatus',
      body: JSON.stringify(payload),
    });
  };

  const markIncomingConnectionsAsSeen = (seenConnectionIds) => {
    const client = clientRef.current;
    // console.log('Marking incoming connections as seen:', seenConnectionIds);
    if (!client?.connected) {
      console.warn('STOMP client not connected - cannot mark incoming connections as seen');
      return;
    }

    if (!seenConnectionIds?.length) return;

    client.publish({
      destination: '/app/receiverHasReadIncomingConnections',
      body: JSON.stringify({ seenConnectionIds }),
    });
  };

  const sendConnectionStatusUpdate = (meId, userId) => {
    const client = clientRef.current;

    if (client && client.connected) {
      client.publish({
        destination: "/app/connection-status-update-request",
        headers: {},
        body: JSON.stringify({ meId, userId }),
      });
    } else {
      console.warn('STOMP client not connected - cannot send connection status update');
    }
  };

  return (
    <StompContext.Provider
      value={{
        messages,
        onlineUsers,
        typingStatus,
        sendMessage,
        sendTypingStatus,
        sendOnlineStatusRequest,
        sendConnectionStatusUpdate,
        setRefreshConnectionStatusFn,
        isConnected,
        errorMessage,
        clearErrorMessage,
        hasUnseenIncomingConnections,
        markIncomingConnectionsAsSeen,
      }}
    >
      {children}
    </StompContext.Provider>
  );
};

export const useStomp = () => useContext(StompContext);
