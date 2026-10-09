import React, { useEffect, useRef } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./NavLinksWindow.css";

const NavLinksWindow = ({ onToggle }) => {
    const navigate = useNavigate();
    const navRef = useRef(null);

    const handleLogout = () => {
        localStorage.removeItem("Authorization");
        navigate("/");
        window.location.reload();
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (navRef.current && !navRef.current.contains(event.target)) {
                onToggle();
            }
        };

        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [onToggle]);

    return (
        <nav ref={navRef} className="nav-links-window">
            <ul>
                <li><Link className="nav-link" to="/">Home</Link></li> 
                <li><Link className="nav-link" to="/profile">Profile</Link></li>
                <li><Link className="nav-link" to="/connections">Connections</Link></li>
                <li>
                    <button className="nav-link logout-btn" onClick={handleLogout}>
                        Logout
                    </button>
                </li>
            </ul>
        </nav>
    );
};

export default NavLinksWindow;