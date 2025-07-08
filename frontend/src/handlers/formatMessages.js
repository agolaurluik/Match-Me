export const formatMessages = (messages) =>
  messages
    .map((msg) => ({
      ...msg,
      sender: msg.sender,
      text: msg.content,
      status: msg.status,
      timestamp: new Date(msg.timestamp),
    }))
    .reverse();