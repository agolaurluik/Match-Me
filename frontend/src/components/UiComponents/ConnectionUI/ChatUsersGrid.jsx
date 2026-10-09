import React, { useState, useMemo } from "react";
import "./ChatUsersGrid.css";
import './aMobile.css';
import ProfileModal from "./ProfileModal";
import { useStomp } from './StompProvider';

const ChatUsersGrid = ({
  connections,
  unreadCounts = {},
  activeUserId,
  onUserClick,
  viewerId,
  updateConnectionStatus,
  onUserRemoved
}) => {
  const [profileUser, setProfileUser] = useState(null);
  const [removingIds, setRemovingIds] = useState([]);


  const handleRemove = (userId) => {
    setRemovingIds(prev => [...prev, userId]);
    setTimeout(() => {
      onUserRemoved(userId);
      setRemovingIds(prev => prev.filter(id => id !== userId));
    }, 300);
  };

  const { onlineUsers } = useStomp();


  // Sort connections by lastMessageTimestamp, newest first
  const sortedConnections = useMemo(() => {
    const normalizedConnections = connections.map(conn => {
      if (typeof conn === 'object') {
        return conn;
      }
      return { friendId: conn, lastMessageTimestamp: 0 };
    });

    const enriched = normalizedConnections.map(conn => {
      const isOnline = onlineUsers[String(conn.friendId)] === "true";
      const unread = unreadCounts[conn.friendId] && conn.friendId !== viewerId
        ? unreadCounts[conn.friendId]
        : 0;

      return {
        ...conn,
        isOnline,
        unread,
        lastMessageTimestamp: conn.lastMessageTimestamp || 0,
      };
    });

    return enriched.sort((a, b) => new Date(b.lastMessageTimestamp) - new Date(a.lastMessageTimestamp));
  }, [connections, onlineUsers, unreadCounts, viewerId]);


  console.log("Chat connection passed to ProfileModal:", profileUser);
  return (
    <>
      <div className="chat-users-grid">
        {connections.length === 0 ? (
          <div>No conversations yet.</div>
        ) : (
          sortedConnections.map((conn) => {
            const isActive = conn.friendId === activeUserId;
            const isBlocked = conn.senderStatus === "BLOCKED" || conn.receiverStatus === "BLOCKED";

            return (
              <div
                key={conn.connectionId}
                className={`chat-user-box ${isActive ? "active" : ""} ${isBlocked ? "blocked" : ""}`}
                onClick={() => onUserClick(conn.friendId)}
              >
                <div className="chat-username">
                  <span
                    className={`online-indicator ${conn.isOnline ? "online" : "offline"}`}
                    title={conn.isOnline ? "Online" : "Offline"}
                  />
                  {conn.username || `User #${conn.friendId}`}
                </div>

                <div className="chat-subline">
                  Last message: {conn.lastMessageTimestamp
                    ? new Date(conn.lastMessageTimestamp).toLocaleString()
                    : " No history..."}
                </div>

                {/* <div>
                  {conn.unread > 0
                    ? `${conn.unread} unread message${conn.unread > 1 ? 's' : ''}` //Not really needed if we have badge for that 
                    : "No unread messages"}
                </div> */}

                {conn.unread > 0 && (
                  <div className="unread-badge">{conn.unread}</div>
                )}

                <button
                  className="user-profile-button"
                  onClick={(e) => {
                    e.stopPropagation();
                    setProfileUser(conn);
                  }}
                  title="View Profile"
                >
                  👤
                </button>
              </div>
            );
          })
        )}
      </div>

      {profileUser && (
        <ProfileModal
          user={profileUser}
          onClose={() => setProfileUser(null)}
          classPrefix={"chat-tab"}
          onUserRemoved={handleRemove}
          updateConnectionStatus={updateConnectionStatus}
        />
      )}
    </>
  );
};

export default ChatUsersGrid;
