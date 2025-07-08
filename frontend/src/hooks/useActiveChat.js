import { useState } from "react";
import api from '../api/api';
import { formatMessages } from "./../handlers/formatMessages";

export const useActiveChat = ({
  connections,
  viewerId,
  setConnections,
  setUnreadCounts
}) => {
  const [activeChatUser, setActiveChatUser] = useState(null);
  const [activeConnectionId, setActiveConnectionId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const refreshSingleConnectionData = async (friendId) => {
    try {
      const newConnData = await api.fetchConnectionDataByUserId(friendId);
      setConnections(prev =>
        prev.map(conn =>
          conn.friendId === friendId
            ? { ...conn, ...newConnData }
            : conn
        )
      );
    } catch (err) {
      console.error(`Failed to refresh connection data for ${friendId}`, err);
    }
  };

  const refreshUnreadCountFor = async (connection) => {
    try {
      const history = await api.fetchChatHistory(connection.connectionId, 0, 50);
      const unreadMessages = history.content.filter(
        msg => msg.status === 'SENT' && msg.receiver === viewerId
      );

      setUnreadCounts(prev => {
        const updated = { ...prev };
        if (unreadMessages.length > 0) {
          updated[connection.friendId] = unreadMessages.length;
        } else {
          delete updated[connection.friendId];
        }
        return updated;
      });
    } catch (err) {
      console.error("Failed to refresh unread count", err);
    }
  };

  const openChatWith = (userId) => {
    const connection = connections.find(c => c.friendId === userId);
    if (!connection) return;

    setActiveChatUser(prevId => {
      const newId = prevId === userId ? null : userId;

      if (newId) {
        api.fetchChatHistory(connection.connectionId, 0, 20)
          .then(history => {
            const formatted = formatMessages(history.content);
            setMessages(formatted);
            setActiveConnectionId(connection.connectionId);
            setPage(1);
            setHasMore(!history.last);

            const newestMessage = formatted[formatted.length - 1];
            const isUnreadMessage = newestMessage &&
              newestMessage.receiver === viewerId;

            if (isUnreadMessage) {
              api.setMessagesAsRead(connection.connectionId).then(async () => {
                setUnreadCounts(prev => {
                  const newCounts = { ...prev };
                  delete newCounts[connection.friendId];
                  return newCounts;
                });

                
                await refreshSingleConnectionData(connection.friendId);
                await refreshUnreadCountFor(connection);
              });
            }
          })
          .catch(err => {
            console.error("Failed to fetch chat history:", err);
          });
      } else {
        setMessages([]);
        setActiveConnectionId(null);
        setPage(0);
        setHasMore(true);
      }

      return newId;
    });
  };

  return {
    activeChatUser,
    activeConnectionId,
    messages,
    setMessages,
    page,
    setPage,
    hasMore,
    setHasMore,
    openChatWith
  };
};
