import './PagesCSS/Pages.css'
import './PagesCSS/BioPage.css'

import { useEffect, useState, useMemo } from 'react';
import Selector from '../components/UiComponents/EditProfilePageUI/Selector';
import Header from '../components/UiComponents/HeaderUI/Header';
import NavLinksWindow from '../components/UiComponents/HeaderUI/NavLinksWindow';
import Button from '../components/UiComponents/GeneralUI/Button';

import useProfile from '../hooks/useProfile';
import useOptions from '../hooks/useOptions';
import { handleProfileSubmit } from '../handlers/handleSubmit';
import { useSecureImage } from '../hooks/useSecureImage';
import { useNavigate } from 'react-router-dom';
import api from '../api/api'


function BioPage() {




    const [nationality, setNationality] = useState();
    const [purpose, setPurpose] = useState();
    const [showNavLinks, setShowNavLinks] = useState(false);
    const [dob, setDob] = useState();
    const [gender, setGender] = useState();
    const [idName, setIdName] = useState();
    const [location, setLocation] = useState({});
    const [profilePic, setProfilePic] = useState(null)
    const [namedLocationId, setNamedLocationId] = useState(null)
    const [locationId, setLocationId] = useState(null)
    const [aboutMeText, setAboutMeText] = useState()
    const navigate = useNavigate();

    //hooks
   
    const { profile: originalData, loading: profileLoading } = useProfile(); //loading screen?
    const { personalities: allPersonalities,
            interests: allInterests,
            nationalities: allNationalities,
            purposes: allPurposes,
            genders: allGenders,
            locations: allLocations 
          } = useOptions();

    const { profile, loading, error } = useProfile();
    const secureImageUrl = useSecureImage(profile?.profileImageName);

    const handleResetProfilePicture = async () => {
      try {
        await api.resetProfilePicture();
        setProfilePic("default-user.jpg"); 
      } catch (error) {
        alert('Failed to reset profile picture: ', error);
        console.error(error);
      }
    };
    

    const sortedInterests = useMemo(() => {
      return [...allInterests].sort((a, b) => a.name.localeCompare(b.name));
    }, [allInterests]);

    const sortedPersonalities = useMemo(() => {
      return [...allPersonalities].sort((a, b) => a.name.localeCompare(b.name));
    }, [allPersonalities]);

    const sortedNationalities = useMemo(() => {
      return [...allNationalities].sort((a, b) => a.name.localeCompare(b.name));
    }, [allNationalities]);

    const sortedPurposes = useMemo(() => {
      return [...allPurposes].sort((a, b) => a.name.localeCompare(b.name));
    }, [allPurposes]);

        const sortedLocations = useMemo(() => {
      return [...allLocations].sort((a, b) => a.name.localeCompare(b.name));
    }, [allLocations]);

    const [interests, setInterests] = useState([]);
    const [personalities, setPersonalities] = useState([]);

useEffect(() => {
  if (profile && secureImageUrl && !profilePic) {
    setProfilePic(secureImageUrl);
  }
}, [profile, secureImageUrl, profilePic]);

useEffect(() => {
  if (profile) {
    setIdName(profile.username || '');

    setDob(profile.birthDate || '');

    setGender(allGenders.find(g => g.id === profile.genderId) || null);

    setNationality(allNationalities.find(n => n.id === profile.nationalityId) || null);

    setPurpose(allPurposes.find(p => p.id === profile.purposeId) || null);

    setAboutMeText(profile.aboutMeText || "")

    setLocation({}); 
    setNamedLocationId(profile.namedLocationId)
    setLocationId(profile.locationId)
  
    setInterests(
      (profile.interestIds || [])
        .map(id => allInterests.find(i => i.id === id))
        .filter(Boolean)
    );

    setPersonalities(
      (profile.personalityIds || [])
        .map(id => allPersonalities.find(p => p.id === id))
        .filter(Boolean)
    );

    setProfilePic(secureImageUrl); 
  }
}, [profile, allGenders, allNationalities, allPurposes, allInterests, allPersonalities, allLocations]);

  if (
    loading ||
    !profile ||
    !allInterests ||
    !allPersonalities ||
    !allNationalities ||
    !allPurposes ||
    !allGenders
  ) {
    return <div className="loading-screen">Loading profile...</div>;
  }
  if (error){<div className="loading-screen">Error Loading profile...</div>;}
    
    return (
      <>
        <Header user={{
          name: profile?.username,
          imageUrl: secureImageUrl,
        }}
        onToggleNavLinks={() => setShowNavLinks(prev => !prev)} />
        {showNavLinks && (<NavLinksWindow/>)}
  
      <div className="biopage-scale-wrapper">
        <div className='biopage-grid'>
          <div className='selector-name'>
            <Selector
            type='name'
            header='Set Name'
            selectedValues={idName}
            onChange={setIdName}
            />
          </div>
          <div className='selector-profile-picture'>
            <Selector
            type='profilePicture'
            header='Set Profile Picture'
            selectedValues={profilePic}
            onChange={(file) => setProfilePic(file)}
            onResetPicture={handleResetProfilePicture}
            />
          </div>
          <div className="selector-aboutme">
            <Selector
              type="aboutMe"
              name="aboutMe"
              selectedValues={aboutMeText}
              onChange={setAboutMeText}
              header="About Me"
            />
          </div>
          <div className='selector-dob'>
            <Selector
              type="dob"
              header="Select Your Date of Birth"
              selectedValues={dob}               
              onChange={(val) => setDob(val)}    
            />
          </div>

          <div className="pair-purposes-nationalities">
            <div className='selector-purposes'>
              <Selector 
                type="optionsSelector"
                header="Select Purpose of Use"
                options={sortedPurposes}
                selectedValues={purpose}
                onChange={setPurpose}
                maxSelection={1}
              />
            </div>
            <div className='selector-nationality'>
              <Selector 
                type="optionsSelector"
                header='Select Nationality'
                options={sortedNationalities}
                selectedValues={nationality}
                onChange={setNationality}
                maxSelection={1}
              />
            </div>
          </div>

          <div className="pair-personalities-interests">
            <div className='selector-interests'>
              <Selector 
                type="optionsSelector"
                header="Select Interests"
                options={sortedInterests}
                selectedValues={interests}
                onChange={setInterests}
                maxSelection={5}
              />
            </div>
            <div className='selector-personalities'>
              <Selector 
                type="optionsSelector"
                header='Select Personality Traits'
                options={sortedPersonalities}
                selectedValues={personalities}
                onChange={setPersonalities}
                maxSelection={5}
              />
            </div>
          </div>

                    <div className='selector-gender'>
            <Selector
              type="gender"
              header="Select Gender"
              options={allGenders}
              selectedValues={gender}
              onChange={setGender}
            />
          </div>

          <div className='selector-location'>
            <Selector
              header="Set Your Location"
              type="location"
              options={sortedLocations}
              selectedValues={location}
              onChange={setLocation}
            />
          </div>
        <Button   onClick={() =>
                handleProfileSubmit({
                  originalData,
                  idName,
                  gender,
                  dob,
                  nationality,
                  purpose,
                  interests,
                  personalities,
                  location,
                  profilePic,
                  namedLocationId,
                  locationId,
                  aboutMeText,
                  navigate
                })
              }
              text={'SUBMIT'} 
              className="selector-submit"/>
        </div>
      </div>
      </>
    );
  }

export default BioPage;