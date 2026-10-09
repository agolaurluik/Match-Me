import React, { useEffect, useRef, useState } from "react";
import ChatBox from "./ChatBox";
import NavLinksWindow from "../HeaderUI/NavLinksWindow";
import api from "../../../api/api";
import './ChatTab.css';
import ChatUsersGrid from "./ChatUsersGrid";
import useFriendConnections from "../../../hooks/useFriendConnections";
import { formatMessages } from "../../../handlers/formatMessages";
import { useStomp } from "./StompProvider";
import { useActiveChat } from "../../../hooks/useActiveChat";
import './aMobile.css';


const ChatTab = () => { //Renders usersgrid and chatbox

  const [showNavLinks, setShowNavLinks] = useState(false);
  const [typingUsers, setTypingUsers] = useState(new Set());

  const rawToken = localStorage.getItem("Authorization");
  const payload = JSON.parse(atob(rawToken.split('.')[1]));
  const viewerId = payload.id;
  

  // -----------------------------------------------------------------------------

  // Hook to load connections and unread counts 

  const {
    connections,
    unreadCounts: connectionUnreadCounts,
    setConnections,
    setUnreadCounts,
    removeChatConnection,
    updateConnectionStatus,
  } = useFriendConnections(viewerId);
// console.log("CONNECTIONS FROM useFriendConnections", connections)



  // -----------------------------------------------------------------------------

  // We use this react element to Stomp the WebSocket connections into the ChatTab component, whether it likes it or not
  // At least that's what the documentation said
  const {
    messages: globalMessages,
    onlineUsers,
    typingStatus,
    unreadCounts: stompUnreadCounts,
    sendMessage: stompSendMessage,
    sendTypingStatus: stompSendTypingStatus,
    sendOnlineStatusRequest,
  } = useStomp();

  // -----------------------------------------------------------------------------

  // Hook to manage active chat logic
  const {
    activeChatUser,
    activeConnectionId,
    messages,
    setMessages,
    page,
    setPage,
    hasMore,
    setHasMore,
    openChatWith
  } = useActiveChat({
    connections,
    viewerId,
    setConnections,
    setUnreadCounts
  });

  // -----------------------------------------------------------------------------
  
  // to disable chat--------------------------------------
  const activeConnection = connections.find(conn => conn.friendId === activeChatUser);


useEffect(() => {
  const lastMessage = globalMessages[globalMessages.length - 1];
  if (!lastMessage) return;

  const senderId = lastMessage.senderId || lastMessage.sender;
  const receiverId = lastMessage.receiverId || lastMessage.receiver;

  // Update timestamp regardless of whether it's the active chat
  const friendId = senderId === viewerId ? receiverId : senderId;
  const timestamp = lastMessage.timestamp || lastMessage.time || new Date().toISOString();

  updateLastMessageTimestamp(friendId, timestamp);

  // If this is the active chat, add the message to the chat view
  if (
    (senderId === activeChatUser && receiverId === viewerId) ||
    (senderId === viewerId && receiverId === activeChatUser)
  ) {
    setMessages((prev) => {
      if (prev.some((m) => m.id === lastMessage.id)) return prev;
      return [
        ...prev,
        {
          sender: senderId,
          receiver: receiverId,
          text: lastMessage.content || lastMessage.text || "",
          timestamp,
          id: lastMessage.id
        }
      ];
    });

    // Mark as read if its from someone else
    if (senderId !== viewerId) {
      api.setMessagesAsRead(lastMessage.connectionId).then(() => {
        setUnreadCounts((prev) => {
          const newCounts = { ...prev };
          delete newCounts[senderId];
          return newCounts;
        });
      });
    }
  } else {
    if (receiverId === viewerId) {
      setUnreadCounts((prev) => ({
        ...prev,
        [senderId]: (prev[senderId] || 0) + 1
      }));
    }
  }
}, [globalMessages]);

  // -----------------------------------------------------------------------------

  useEffect(() => {

    if (connections && connections.length > 0) {
      if (activeChatUser) {
        const activeConnection = connections.find(conn => conn.friendId === activeChatUser);
        if (activeConnection) {
          sendOnlineStatusRequest(viewerId, connections.map(c => c.friendId));
        }
      } else {
        sendOnlineStatusRequest(viewerId, connections.map(c => c.friendId));
      }
    }
  }, [connections, activeChatUser]);


  const loadMessages = (connectionId, pageIndex = 0) => {
    api.fetchChatHistory(connectionId, pageIndex, 20)
      .then((history) => {
        const formatted = formatMessages(history.content);
        setMessages((prev) => (pageIndex === 0 ? formatted : [...formatted, ...prev]));
        setPage(pageIndex + 1);
        setHasMore(!history.last);
      })
      .catch((err) => console.error("Failed to fetch messages:", err));
  };

  const lastSentRef = useRef(0);


  const loadOlderMessages = () => {
    const now = Date.now();
    if (now - lastSentRef.current < 500) {
      // console.log("Please wait before sending another message.");
      return;
    }
    if (!activeConnectionId || !hasMore) return;
    loadMessages(activeConnectionId, page);
  };


  const sendMessage = (text) => {
    if (!activeChatUser) return;


    // console.log("[ChatTab] Sending message:", {
    //   sender: viewerId,
    //   receiver: activeChatUser,
    //   text
    // });

    stompSendMessage(viewerId.toString(), activeChatUser.toString(), text);

    updateLastMessageTimestamp(activeChatUser, new Date().toISOString());
  };

  const updateLastMessageTimestamp = (friendId, timestamp) => {
  setConnections(prev => 
    prev.map(conn => 
      conn.friendId === friendId
        ? { ...conn, lastMessageTimestamp: timestamp }
        : conn
    )
  );
};

  // Typing timeout handling
  const typingTimeouts = useRef({});

  const handleTyping = (isTyping) => {
    if (!activeChatUser || !viewerId) return;

    if (isTyping) {
      setTypingUsers((prev) => new Set(prev.add(activeChatUser))); // Add to typing users
    } else {
      setTypingUsers((prev) => {
        const updated = new Set(prev);
        updated.delete(activeChatUser); // Remove from typing users
        return updated;
      });
    }

    // Clear the previous timeout and set a new one
    if (typingTimeouts.current[activeChatUser]) {
      clearTimeout(typingTimeouts.current[activeChatUser]);
    }

    // Set timeout to remove the user from typing list after 2 seconds of inactivity
    typingTimeouts.current[activeChatUser] = setTimeout(() => {
      setTypingUsers((prev) => {
        const updated = new Set(prev);
        updated.delete(activeChatUser);
        return updated;
      });
    }, 3000);

    // Send the typing status to the server
    stompSendTypingStatus(viewerId, activeChatUser, isTyping);
  };

  return (
    <>
      {showNavLinks && <NavLinksWindow />}
      <div className="page-grid">
        <aside className="sidebar">
          <ChatUsersGrid
            connections={connections}
            unreadCounts={connectionUnreadCounts}
            activeUserId={activeChatUser}
            onUserClick={openChatWith}
            onlineUsers={onlineUsers}
            onUserRemoved={(id) => {
            removeChatConnection(id);
            }}
            updateConnectionStatus={updateConnectionStatus}
          />
        </aside>
        <main className="chat-main">

          {activeChatUser && (
            <ChatBox
              messages={messages}
              onSendMessage={sendMessage}
              activeChatUser={activeChatUser}
              viewerId={viewerId}
              onLoadMore={loadOlderMessages}
              onTyping={handleTyping}
              isTyping={typingStatus[activeChatUser]}
              connections={connections}
              activeConnection={activeConnection}
            />
          )}
        </main>
      </div>
    </>
  );
};

export default ChatTab;
