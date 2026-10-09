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
    const [activeModal, setActiveModal] = useState(null);

    const location = useLocation();

    const [authToken, setAuthToken] = useState(
        localStorage.getItem('Authorization')
    );
    const { profile } = useProfile(authToken);

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
                        ? () => setActiveModal('register')
                        : undefined
                }
                onLoginClick={
                    isFrontpage
                        ? () => setActiveModal('login')
                        : undefined
                }

                onToggleNavLinks={toggleNavLinks}
            />

            {showNavLinks && (
                <NavLinksWindow onToggle={toggleNavLinks} />
            )}

            <Outlet
                context={{
                    onRegisterClick: () => setActiveModal('register'),
                    isLoggedIn: Boolean(authToken),
                }}
            />

            {activeModal === 'register' && (
                <RegisterModal
                    onClose={() => setActiveModal(null)}
                    onRegisterSuccess={(token) => setAuthToken(token)}
                />
            )}

            {activeModal === 'login' && (
                <LoginModal
                    onClose={() => setActiveModal(null)}
                    onLoginSuccess={(token) => setAuthToken(token)}
                />
            )}
        </>
    );
};

export default PageLayout;