import { Stomp } from "@stomp/stompjs";

let stompClient = null;

export const createStompClient = (token, onConnect, onError) => {
  const cleanedToken = token.replace("Bearer ", "");
  const socketUrl = `ws://localhost:8080/ws?token=${cleanedToken}`;
  const socket = () => new WebSocket(socketUrl);

  stompClient = Stomp.over(socket);
  stompClient.connect(
    { Authorization: token },
    onConnect,
    onError
  );

  return stompClient;
};

export const getStompClient = () => stompClient;

export const disconnectStompClient = () => {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
    stompClient = null;
  }
};