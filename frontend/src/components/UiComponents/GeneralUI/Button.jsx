import React from 'react';
import './Button.css'; 

const Button = ({ onClick, children, text, className = '' }) => {
  return (
    <button
      onClick={onClick}
      className={`animated-glow-btn ${className}`}
    >
      {text}
      {children}
    </button>
  );
};

export default Button;