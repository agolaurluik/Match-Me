import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';

import Header from '../components/UiComponents/HeaderUI/Header';
import NavLinksWindow from '../components/UiComponents/HeaderUI/NavLinksWindow';

import RegisterModal from '../components/Modals/RegisterModal';
import LoginModal from '../components/Modals/LoginModal';

import useProfile from '../hooks/useProfile';
import { useSecureImage } from '../hooks/useSecureImage';

const PageLayout = () => {
    const [showNavLinks, setShowNavLinks] = useState(false);
    const [showRegister, setShowRegister] = useState(false);
    const [showLogin, setShowLogin] = useState(false);

    const location = useLocation();

    const { profile } = useProfile();
    const secureImageUrl = useSecureImage(profile?.profileImageName);

    const isFrontpage = location.pathname === '/';

    const toggleNavLinks = () => {
        setShowNavLinks(prev => !prev);
    };

    return (
        <>
            <Header
                user={
                    profile
                        ? {
                            name: profile.username,
                            imageUrl: secureImageUrl,
                        }
                        : null
                }
                onRegisterClick={
                    isFrontpage
                        ? () => setShowRegister(true)
                        : undefined
                }
                onLoginClick={
                    isFrontpage
                        ? () => setShowLogin(true)
                        : undefined
                }
                onToggleNavLinks={toggleNavLinks}
            />

            {showNavLinks && (
                <NavLinksWindow onToggle={toggleNavLinks} />
            )}

            <Outlet />

            {showRegister && (
                <RegisterModal
                    onClose={() => setShowRegister(false)}
                />
            )}

            {showLogin && (
                <LoginModal
                    onClose={() => setShowLogin(false)}
                />
            )}
        </>
    );
};

export default PageLayout;