export const formatMessages = (messages = []) =>
  messages
    .map((msg) => ({
      ...msg,
      sender: msg.senderId ?? msg.sender,
      receiver: msg.receiverId ?? msg.receiver,
      text: msg.content ?? msg.text,
      status: msg.status,
      timestamp: new Date(msg.timestamp ?? msg.time),
    }))
    .reverse();
