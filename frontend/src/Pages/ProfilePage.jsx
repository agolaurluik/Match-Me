import './PagesCSS/Pages.css'

import ProfilePicture from '../components/UiComponents/GeneralUI/ProfilePicture';
import ProfileDataBlock from '../components/UiComponents/ProfilePageUI/ProfileDataBlock';
import { useEffect, useState } from 'react';
import NavLinksWindow from '../components/UiComponents/HeaderUI/NavLinksWindow';
import Header from '../components/UiComponents/HeaderUI/Header';
import { useNavigate } from 'react-router-dom';
import './PagesCSS/ProfilePage.css'
import Button from '../components/UiComponents/GeneralUI/Button';
import useProfile from '../hooks/useProfile';
import useOptions from '../hooks/useOptions';
import { useSecureImage } from '../hooks/useSecureImage';

const findNameById = (list, id) => {
  if (!id || !Array.isArray(list)) return null;
  const found = list.find(item => item.id === id);
  return found?.name || null;
};
    
    const ProfilePage = () => {
        const { profile, loading, error } = useProfile();
        const secureImageUrl = useSecureImage(profile?.profileImageName);
        const {
            personalities: allPersonalities = [],
            interests: allInterests = [],
            nationalities: allNationalities = [],
            purposes: allPurposes = [],
            genders: allGenders = [],
        } = useOptions();
        
        const [showNavLinks, setShowNavLinks] = useState(false);
        const navigate = useNavigate();

        const handleEdit = () => {
            navigate('/bio');
        };
        

        const optionsLoaded =
            allPersonalities.length > 0 &&
            allInterests.length > 0 &&
            allNationalities.length > 0 &&
            allPurposes.length > 0 &&
            allGenders.length > 0;

        if (loading || !profile || !optionsLoaded) {
            return <div className="loading">Loading profile...</div>;
        }
        if (error) {
            return <div className="error">Failed to load profile.</div>;
        }

        const ageString = profile.birthDate
            ? Math.floor((Date.now() - profile.birthDate) / (1000 * 60 * 60 * 24 * 365.25)) + ' years'
            : '';

        const user = {
            name: profile.username,

            age: profile.birthDate
            ? [Math.floor((Date.now() - new Date(profile.birthDate)) / (1000 * 60 * 60 * 24 * 365.25)) + ' years old']
            : [],

            gender: findNameById(allGenders, profile.genderId),
            profilePicture: profile.profileImageName || null,
            purposeOfUse: [findNameById(allPurposes, profile.purposeId)],
            interests: (profile.interestIds || []).map(id => findNameById(allInterests, id)),
            personalities: (profile.personalityIds || []).map(id => findNameById(allPersonalities, id)),
            nationality: [findNameById(allNationalities, profile.nationalityId)],
            aboutMeText: profile.aboutMeText ? [profile.aboutMeText] : [],
            email: profile.email || null
        };
        // console.log(user)

return (
    <>
      <Header
        user={{ name: user.name, imageUrl: secureImageUrl }}
        onToggleNavLinks={() => setShowNavLinks(prev => !prev)}
      />
      {showNavLinks && <NavLinksWindow />}

      <div className="grid-container">
        <div className="grid-container-wrapper">
          <div className="profile-grid">
            <div className="profile-section pic">
              
            
            <ProfileDataBlock header={user.name} email={user.email} profileImage={secureImageUrl} displayMode="label-value" items={[]} />
            </div>

            <div className="profile-section abo">
              <ProfileDataBlock header="About-Me" items={user.aboutMeText} displayMode="label-value" />
            </div>
            
            <div className="profile-section age">
              <ProfileDataBlock header="Age" items={user.age} displayMode="label-value" />
            </div>

            <div className="profile-section gen">
              <ProfileDataBlock header="Gender" items={user.gender} displayMode="label-value" />
            </div>

            <div className="profile-section pou">
              <ProfileDataBlock header="Purpose" items={user.purposeOfUse} displayMode="inline" />
            </div>

            <div className="profile-section nat">
              <ProfileDataBlock header="Nationality" items={user.nationality} displayMode="inline" />
            </div>
            <div className="profile-section per">
              <ProfileDataBlock header="Personalities" items={user.personalities} displayMode="inline" />
            </div>

            <div className="profile-section int">
              <ProfileDataBlock header="Interests" items={user.interests} displayMode="inline" />
            </div>
          

            <div className="edb">
              <div className="button-wrapper">
                <Button onClick={handleEdit} text="Edit" className="profile-edit-btn" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};




export default ProfilePage;