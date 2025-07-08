import React, { useRef, useState, useEffect } from 'react';
import './ChatBox.css';
import './aMobile.css';

const ChatBox = ({ messages, onSendMessage, onLoadMore, activeChatUser, viewerId, onTyping, isTyping, connections, activeConnection  }) => {

  const [newMessage, setNewMessage] = useState("");
  const typingTimeoutRef = useRef(null);
  const [input, setInput] = useState('');

  const messagesContainerRef = useRef(null);
  const messagesEndRef = useRef(null);
  const textareaRef = useRef(null);
  const isBlocked = activeConnection?.senderStatus === 'BLOCKED' || activeConnection?.receiverStatus === 'BLOCKED';
  const emojiList = ['😀', '😂', '😍', '👍', '🙏', '😢', '🔥', '💯', '🎉', '😎']; //Maybe will use them later..

  // Track whether we should auto-scroll (only when at bottom)
  const shouldAutoScrollRef = useRef(true);

  // Check scroll position to decide whether to auto-scroll on new messages
  const handleScroll = (e) => {
    if (e.target.scrollTop === 0) {
      if (onLoadMore) {
        onLoadMore();
      }
    }

    const isAtBottom = Math.abs(e.target.scrollHeight - e.target.scrollTop - e.target.clientHeight) < 5;
    shouldAutoScrollRef.current = isAtBottom;
  };
  
  const getUsernameById = (id) => {
    if (id === viewerId) return 'You';
    const user = connections?.find(c => c.friendId === id);
    return user?.username || `User #${id}`;
  };

  // Scroll to bottom when new messages arrive if user was at bottom before
  useEffect(() => {
    if (shouldAutoScrollRef.current && messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'auto' });
    }
  }, [messages, isTyping]);

  // Auto-resize textarea as user types
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }
  }, [input]);

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
      month: 'numeric',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  };

const handleSend = () => {
  const trimmed = input.trim();
  if (!trimmed) return;
  
  onSendMessage(trimmed);

  setInput('');

  if (onTyping) onTyping(false);
};

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };
const handleTyping = (e) => {
    const value = e.target.value;
    setInput(value);

    
    if (onTyping && !isTyping) {
      onTyping(true);
    }
  };

  return (
    <div className="chatbox">
      <div
        className="chatbox-messages"
        ref={messagesContainerRef}
        onScroll={handleScroll}
      >
        {messages.map((msg, i) => (
          <div key={i} className={`chat-message ${msg.sender === viewerId ? 'user' : 'bot'}`}>

        <div className="chat-meta">
        {getUsernameById(msg.sender)} — {formatDate(msg.timestamp)}
      </div>

            <div className="chat-text">{msg.text}</div>

          </div>
        ))}

        {isTyping && (
            <div className="typing-indicator">
              {`User is typing...`}
            </div>
          )}

        <div ref={messagesEndRef} />
      </div>

      <div className="chatbox-input">
        <textarea
          ref={textareaRef}
          value={input}
          onChange={handleTyping}
          onKeyDown={handleKeyDown}
          placeholder={isBlocked ? "You can't message this user." : "Type your message..."}
          disabled={isBlocked}
          rows={1}
        />
        <button onClick={handleSend}>Send</button>
      </div>
    </div>
  );
}

export default ChatBox;