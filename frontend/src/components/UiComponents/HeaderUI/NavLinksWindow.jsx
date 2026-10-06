import React, { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
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
                <li><a className="nav-link" href="/">Home</a></li>
                <li><a className="nav-link" href="profile">Profile</a></li>
                <li><a className="nav-link" href="connections">Connections</a></li>
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