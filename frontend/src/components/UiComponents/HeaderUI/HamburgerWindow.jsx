import React from "react";

//For NavLinksWindow

const HamburgerWindow = ({ onToggle }) => {
  return (
    <button className="hamburger-button" onClick={onToggle} aria-label="Toggle navigation menu">
      <div className="hamburger">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </button>
  );
};

export default HamburgerWindow;