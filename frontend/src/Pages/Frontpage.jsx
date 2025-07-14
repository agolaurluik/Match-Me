import './PagesCSS/Pages.css'
import './PagesCSS/FrontPage.css'


import InfoBox from '../components/UiComponents/FrontPageUI/InfoBox'
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

  const frontPage = true;

  const isMobile = screenSize === 'mobile';
  const isTablet = screenSize === 'tablet';
  const isDesktop = screenSize === 'desktop';

  useEffect(() => {
    const handleResize = () => {
      window.scrollTo({
        top: 0,
        behavior: 'instant',
      });
      setCurrentPage(0);
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('Authorization');
    setAuthToken(token);
  }, []);

  const { profile, loading, error } = useProfile(authToken, frontPage);
  const secureImageUrl = useSecureImage(profile?.profileImageName);

  
  const user = profile
    ? {
        username: profile.username,
        profilePicture: profile.profilePictureUrl,
      }
    : null;

  useEffect(() => {
    const handleWheel = (e) => {
      if (showRegister || showLogin || isAnimating) return;
      e.preventDefault();
      const direction = e.deltaY > 0 ? 1 : -1;
      let nextPage = Math.max(0, Math.min(TOTAL_SECTIONS - 1, currentPage + direction));
      if (nextPage === currentPage) return;

      setCurrentPage(nextPage);
      setIsAnimating(true);
      window.scrollTo({ top: nextPage * window.innerHeight, behavior: 'smooth' });
      setTimeout(() => setIsAnimating(false), 500);
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
      let nextPage = Math.max(0, Math.min(TOTAL_SECTIONS - 1, currentPage + direction));
      if (nextPage === currentPage) return;

      setCurrentPage(nextPage);
      setIsAnimating(true);
      document.querySelectorAll('.info-section')[nextPage]?.scrollIntoView({ behavior: 'smooth' });
      setTimeout(() => setIsAnimating(false), 500);
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

  const sections = [
  {
    title: "Welcome to WingLINK",
    text: "Choose why you're here — whether it's to make new friends, find collaborators, connect over shared hobbies, or explore something more. Your intent helps us find the right kind of people for you.",
  },
  {
    title: "Interests",
    text: "Let us know what you're into! From gaming and fitness to art and tech, your interests help us match you with people who genuinely vibe with your world.",
  },
  {
    title: "Personality",
    text: "Pick a few personality tags that reflect who you are — like outgoing, chill, ambitious, or deep thinker. This adds a personal touch to your profile and helps others connect beyond surface level.",
  },
  {
    title: "Location Range",
    text: "Set how far you're willing to connect — whether you're looking for people nearby or open to long-distance chats. The location range keeps your connections practical and meaningful.",
  },
  {
    title: "Ready to connect?",
    text: "Join the community and start matching with people who share your interests and personality.",
    hasButton: true,
  },
];

const sectionImages = [
  ['heart.png'],
  ['book.png', "football.png"],
  ['brain.png'],
  ['distance.png'],
  ['connections.png'],
];

  return (
    <>
    <div className="frontpage-wrapper">
      <Header
        user={user ? { name: user.username, imageUrl: secureImageUrl } : null}
        onRegisterClick={() => setShowRegister(true)}
        onLoginClick={() => setShowLogin(true)}
        onToggleNavLinks={() => setShowNavLinks((prev) => !prev)}
      />
      {showNavLinks && <NavLinksWindow />}

      <div
        style={{
          maxWidth: '90vw',
          margin: '0 auto',
          padding: '2rem',
          position: 'relative',
          zIndex: 1,
        }}
      >
       {sections.map((section, index) => (
  <div
    key={index}
    className={`info-section ${index === currentPage ? 'active' : ''}`}
  >
    <InfoBox className = "info-content" side="left" title={section.title} text={section.text}>
      {section.hasButton && (
        <button
          className="register-button"
          onClick={() => setShowRegister(true)}
        >
          Register Now
        </button>
      )}
    </InfoBox>

    <div
      className="info-image-wrapper"
      data-count={
        isMobile ? 1 : sectionImages[index].length
      }
    >
      {(isMobile ? [sectionImages[index][0]] : sectionImages[index]).map((src, i) => (
        <img key={i} src={src} alt={`Section ${index + 1} image ${i + 1}`} className="info-image" />
      ))}
    </div>
  </div>
))}
      </div>

      {showRegister && <RegisterModal onClose={() => setShowRegister(false)} />}
      {showLogin && <LoginModal onClose={() => setShowLogin(false)} />}
      </div>
    </>
  );
}

export default Frontpage;
