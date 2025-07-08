import React, {useState, useEffect } from "react";  
import './PagesCSS/Pages.css'
import Header from "../components/UiComponents/HeaderUI/Header";
import NavLinksWindow from '../components/UiComponents/HeaderUI/NavLinksWindow';
import DashBoard from "../components/UiComponents/ConnectionUI/DashBoard";
import useProfile from '../hooks/useProfile';
import { useSecureImage } from '../hooks/useSecureImage';
import { useNavigate } from 'react-router-dom';
import { StompProvider } from "../components/UiComponents/ConnectionUI/StompProvider";



const ConnectionsPage = () => {

   
    const [showNavLinks, setShowNavLinks] = useState(false);
    const { profile, loading: profileLoading, error: profileError } = useProfile();
    const [countdown, setCountdown] = useState(5);
    const navigate = useNavigate();
    
    const profileSecureImageUrl = useSecureImage(profile?.profileImageName);

      const isProfileComplete = (profile) => {
    if (!profile) return false;

    const {
      aboutMeText,
      birthDate,
      genderId,
      interestIds,
      locationId,
      namedLocationId,
      nationalityId,
      personalityIds,
      profileImageName,
      purposeId,
      username
    } = profile;

    const hasLocation = locationId !== null || namedLocationId !== null;

    return (
      aboutMeText !== null &&
      birthDate !== null &&
      genderId !== null &&
      interestIds?.length > 0 &&
      hasLocation &&
      nationalityId !== null &&
      personalityIds?.length > 0 &&
      profileImageName !== null &&
      purposeId !== null &&
      username !== null
    );
  };

    useEffect(() => {
    if (!profile || isProfileComplete(profile)) return;

    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          navigate('/bio');
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [profile, navigate]);

  if (profileLoading ) {
    return <div className="loading-container">Loading your profile...</div>;
  }

  if (profileError) {
    return <div className="error-container">Failed to load profile data.</div>;
  }

    if (!isProfileComplete(profile)) {
    return (
      <div className="incomplete-profile-container">
        <h2>Complete your profile to start connecting with others!</h2>
        <p>Hang tight — redirecting to your profile settings in {countdown} seconds...</p>
      </div>
    );
  }

  // console.log(profile)

    return (
       <StompProvider>
      <>
        <Header user={{
          name: profile.username,
          imageUrl: profileSecureImageUrl,
        }}
        onToggleNavLinks={() => setShowNavLinks(prev => !prev)} />
        {showNavLinks && (<NavLinksWindow/>)}
        <div className="page-wrapper">
          <DashBoard/>
        </div>
      </>
      </StompProvider>
    );
  }

export default ConnectionsPage;