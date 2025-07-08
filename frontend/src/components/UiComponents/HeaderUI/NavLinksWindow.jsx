import React from "react";
import { useNavigate } from "react-router-dom";
import "./NavLinksWindow.css"

const NavLinksWindow = () => {

    const navigate = useNavigate();
    
    const handleLogout = () => {
        localStorage.removeItem('Authorization');
        navigate('/'); 
        window.location.reload(); 
    };
    return (
    <nav className="nav-links-window">
        <ul>
            <li><a className="nav-link" href="/">Home</a></li>
            <li><a className="nav-link" href="profile">Profile</a></li>
            <li><a className="nav-link" href="connections">Connections</a></li>
            <li><button className="nav-link logout-btn" onClick={handleLogout}>Logout</button></li>
        </ul>
    </nav>
    )
}

export default NavLinksWindow;