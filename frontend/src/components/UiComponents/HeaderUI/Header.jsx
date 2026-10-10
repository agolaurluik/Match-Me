import React from "react";
import { Link, useNavigate } from "react-router-dom";

import HamburgerWindow from "./HamburgerWindow";
import ProfilePicture from "../GeneralUI/ProfilePicture";
import "./Header.css";

const Header = ({
  user,
  onRegisterClick,
  onLoginClick,
  onToggleNavLinks
}) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("Authorization");
    navigate("/");
    window.location.reload();
  };

  return (
    <div className="frontpage-header">
      <div className="header-content">
        <div className="logo-container">
          <Link to="/" className="logo-link">
            <img
              src="/bird.png"
              alt="WingLink Logo"
              className="small-logo"
            />

            <h1 className="multicolor-logo">
              <span className="c1">W</span>
              <span className="c2">i</span>
              <span className="c3">n</span>
              <span className="c4">g</span>
              <span className="c5">L</span>
              <span className="c6">I</span>
              <span className="c7">N</span>
              <span className="c8">K</span>
            </h1>
          </Link>
        </div>

        {user ? (
          <>
            {/* Desktop navigation */}
            <nav className="desktop-nav">
              <Link className="desktop-nav-link" to="/">
                Home
              </Link>

              <Link className="desktop-nav-link" to="/profile">
                Profile
              </Link>

              <Link className="desktop-nav-link" to="/connections">
                Connections
              </Link>

              <button
                className="desktop-nav-link desktop-logout-btn"
                onClick={handleLogout}
              >
                Logout
              </button>
            </nav>

            <div className="header-buttons">
              <Link to="/profile" className="user-info-link">
                <div className="user-info">
                  <ProfilePicture
                    imageUrl={user.imageUrl || "default-user.jpg"}
                    size={40}
                  />

                  <span className="user-name">
                    {user.name || "User"}
                  </span>
                </div>
              </Link>

              {/* Mobile and tablet navigation */}
              <div className="mobile-nav-toggle">
                <HamburgerWindow onToggle={onToggleNavLinks} />
              </div>
            </div>
          </>
        ) : (
          <div className="header-buttons">
            {onRegisterClick && onLoginClick && (
              <>
                <button
                  className="header-btn"
                  onClick={onRegisterClick}
                >
                  Register
                </button>

                <button
                  className="header-btn"
                  onClick={onLoginClick}
                >
                  Login
                </button>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Header;