


import './PagesCSS/Pages.css'


import InfoBox from '../components/UiComponents/FrontPageUI/InfoBox'
import Scene from '../components/ThreejsComponents/Scene';
import { useEffect, useState } from 'react';
import RegisterModal from '../components/Modals/RegisterModal';
import LoginModal from '../components/Modals/LoginModal';
import NavLinksWindow from '../components/UiComponents/HeaderUI/NavLinksWindow';
import useProfile from '../hooks/useProfile';
import Header from '../components/UiComponents/HeaderUI/Header';
import { useSecureImage } from '../hooks/useSecureImage';
import useScreenSize from '../hooks/useScreenSize';

const TOTAL_SECTIONS = 5;

function Frontpage() {
  
  const [currentPage, setCurrentPage] = useState(0);
  const [isAnimating, setIsAnimating] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [showNavLinks, setShowNavLinks] = useState(false);
  const [authToken, setAuthToken] = useState(null);
  const screenSize = useScreenSize();

  const frontPage = true //makeshift fix for frontPage profile fetch errors without token xD

  const isMobile = screenSize === 'mobile';
  const isTablet = screenSize === 'tablet';
  const isDesktop = screenSize === 'desktop'

  const textStyle = {
    fontSize: isMobile ? '1.2rem' : '2rem',
    padding: isMobile ? '20px' : '40px',
  };

  const meshPosition = isMobile ? [0, 0, 2] : [0, 0, 5];

  useEffect(() => {
    const handleResize = () => {
      window.scrollTo({
        top: 0,
        behavior: 'instant', 
      });
      setCurrentPage(0); 
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('Authorization');
    setAuthToken(token);
  }, []);

  const { profile, loading, error } = useProfile(authToken, frontPage); 

  const secureImageUrl = useSecureImage(profile?.profileImageName);

  const user = profile ? {
    username: profile.username,
    profilePicture: profile.profilePictureUrl,
  } : null;


useEffect(() => {
  const handleWheel = (e) => {
    if (showRegister || showLogin || isAnimating) return;

    e.preventDefault();

    const direction = e.deltaY > 0 ? 1 : -1;
    let nextPage = currentPage + direction;
    nextPage = Math.max(0, Math.min(TOTAL_SECTIONS - 1, nextPage));
    if (nextPage === currentPage) return;

    setCurrentPage(nextPage);
    setIsAnimating(true);

    window.scrollTo({
      top: nextPage * window.innerHeight,
      behavior: 'smooth',
    });

    setTimeout(() => {
      setIsAnimating(false);
    }, 500);
  };

  let touchStartY = 0;
  let touchEndY = 0;

  const handleTouchStart = (e) => {
    if (showRegister || showLogin) return;
    touchStartY = e.touches[0].clientY;
  };

  const handleTouchMove = (e) => {
    if (showRegister || showLogin) return;
    e.preventDefault(); 
    touchEndY = e.touches[0].clientY;
  };

  const handleTouchEnd = () => {
    if (showRegister || showLogin || isAnimating) return;

    const swipeDistance = touchStartY - touchEndY;
    if (Math.abs(swipeDistance) < 50) return; 

    const direction = swipeDistance > 0 ? 1 : -1;
    let nextPage = currentPage + direction;
    nextPage = Math.max(0, Math.min(TOTAL_SECTIONS - 1, nextPage));
    if (nextPage === currentPage) return;

    setCurrentPage(nextPage);
    setIsAnimating(true);

    window.scrollTo({
      top: nextPage * window.innerHeight,
      behavior: 'smooth',
    });

    setTimeout(() => {
      setIsAnimating(false);
    }, 500);
  };

  window.addEventListener('wheel', handleWheel, { passive: false });
  window.addEventListener('touchstart', handleTouchStart, { passive: false });
  window.addEventListener('touchmove', handleTouchMove, { passive: false });
  window.addEventListener('touchend', handleTouchEnd);

  return () => {
    window.removeEventListener('wheel', handleWheel);
    window.removeEventListener('touchstart', handleTouchStart);
    window.removeEventListener('touchmove', handleTouchMove);
    window.removeEventListener('touchend', handleTouchEnd);
  };
}, [currentPage, isAnimating, showRegister, showLogin]);
  
  return (
    <>
    <Header
      user={user ? {
        name: user.username,
        imageUrl: secureImageUrl,
      } : null}
      onRegisterClick={() => setShowRegister(true)}
      onLoginClick={() => setShowLogin(true)}
      onToggleNavLinks={() => setShowNavLinks(prev => !prev)}
    />
  {showNavLinks && ( <NavLinksWindow/> )}
      
<Scene    meshPosition={meshPosition}
          isMobile={isMobile}
          isTablet={isTablet}
          isDesktop={isDesktop}

          earthVisible={currentPage === 3}
          heartVisible={currentPage === 0}
          orbitGroupVisible={currentPage === 1}
          textVisible={currentPage === 2}
          connectTextVisible={currentPage === 4}
          /> 

            <div
        style={{
          maxWidth: '90vw',
          margin: '0 auto',
          padding: '2rem',
          position: 'relative',
          zIndex: 1, 
        }}
      >
      <InfoBox
        side="left"
        title="Welcome to WingLINK"
        text="Choose why you're here — whether it's to make new friends, find collaborators, connect over shared hobbies, or explore something more. Your intent helps us find the right kind of people for you."
      />
      <InfoBox
        side="left"
        title="Interests"
        text="Let us know what you're into! From gaming and fitness to art and tech, your interests help us match you with people who genuinely vibe with your world."
      />
      <InfoBox
        side="left"
        title="Personality"
        text="Pick a few personality tags that reflect who you are — like outgoing, chill, ambitious, or deep thinker. This adds a personal touch to your profile and helps others connect beyond surface level."
      />
      <InfoBox
        side="left"
        title="Location Range"
        text="Set how far you're willing to connect — whether you're looking for people nearby or open to long-distance chats. The location range keeps your connections practical and meaningful."
      />
      <InfoBox
        side="left"
        title="Ready to connect?"
        text="Join the community and start matching with people who share your interests and personality."
      >
          <button className="register-button" onClick={() => setShowRegister(true)}>
            Register Now
          </button>
        </InfoBox>
      
    </div>
    {showRegister && <RegisterModal onClose={() => setShowRegister(false)} />} 
    {showLogin && <LoginModal onClose={() => setShowLogin(false)} />} 
    </>
  );
}

export default Frontpage
