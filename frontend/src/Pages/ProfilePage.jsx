import './PagesCSS/Pages.css';
import './PagesCSS/ProfilePage.css';

import { useNavigate } from 'react-router-dom';
import ProfileDataBlock from '../components/UiComponents/ProfilePageUI/ProfileDataBlock';
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
    
    const authToken = localStorage.getItem('Authorization');
    const { profile, loading, error } = useProfile(authToken);

    const secureImageUrl = useSecureImage(profile?.profileImageName);

    const {
        personalities: allPersonalities = [],
        interests: allInterests = [],
        nationalities: allNationalities = [],
        purposes: allPurposes = [],
        genders: allGenders = [],
    } = useOptions();

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

    const age = profile.birthDate
        ? Math.floor(
            (Date.now() - new Date(profile.birthDate)) /
            (1000 * 60 * 60 * 24 * 365.25)
        )
        : null;

    const user = {
        name: profile.username,
        age,
        gender: findNameById(allGenders, profile.genderId),
        purpose: findNameById(allPurposes, profile.purposeId),
        nationality: findNameById(allNationalities, profile.nationalityId),

        interests: (profile.interestIds || [])
            .map(id => findNameById(allInterests, id))
            .filter(Boolean),

        personalities: (profile.personalityIds || [])
            .map(id => findNameById(allPersonalities, id))
            .filter(Boolean),

        aboutMe: profile.aboutMeText || '',
        email: profile.email || null,
    };

    return (
        <>

            <main className="profile-page">
                <section className="profile-card">

                    {/* Profile header */}
                    <header className="profile-card-header">
                        <div className="profile-identity">
                            <div className="profile-picture-wrapper">
                                <img
                                    src={secureImageUrl}
                                    alt={`${user.name}'s profile`}
                                    className="profile-picture"
                                />
                            </div>

                            <div className="profile-identity-info">
                                <h1>{user.name}</h1>

                                {user.email && (
                                    <p className="profile-email">
                                        {user.email}
                                    </p>
                                )}
                            </div>
                        </div>

                        <Button
                            onClick={handleEdit}
                            text="Edit"
                            className="profile-edit-btn"
                        />
                    </header>


                    {/* About */}
                    <section className="profile-section profile-about">
                        <ProfileDataBlock
                            header="About me"
                            items={user.aboutMe ? [user.aboutMe] : []}
                            displayMode="label-value"
                        />
                    </section>


                    {/* Basic information */}
                    <section className="profile-section">
                        <h2>Basic information</h2>

                        <div className="profile-info-grid">
                            <ProfileDataBlock
                                header="Age"
                                items={user.age !== null ? [`${user.age} years old`] : []}
                                displayMode="label-value"
                            />

                            <ProfileDataBlock
                                header="Gender"
                                items={user.gender ? [user.gender] : []}
                                displayMode="label-value"
                            />

                            <ProfileDataBlock
                                header="Nationality"
                                items={user.nationality ? [user.nationality] : []}
                                displayMode="label-value"
                            />

                            <ProfileDataBlock
                                header="Purpose"
                                items={user.purpose ? [user.purpose] : []}
                                displayMode="label-value"
                            />
                        </div>
                    </section>


                    {/* Interests and personality */}
                    <section className="profile-section">
                        <h2>Interests & personality</h2>

                        <div className="profile-tags-grid">
                            <ProfileDataBlock
                                header="Interests"
                                items={user.interests}
                                displayMode="inline"
                            />

                            <ProfileDataBlock
                                header="Personalities"
                                items={user.personalities}
                                displayMode="inline"
                            />
                        </div>
                    </section>

                </section>
            </main>
        </>
    );
};


export default ProfilePage;

