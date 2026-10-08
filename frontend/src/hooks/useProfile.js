import { useEffect, useState } from 'react';
import api from '../api/api';

export default function useProfile(authToken, frontPage = false) {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!authToken) {
      setLoading(false);
      setProfile(null);
      return;
    }

    async function fetchProfile() {
      try {
        const [userDataResponse, bioDataResponse, profileDataResponse] = await Promise.all([
          api.fetchMeById(),
          api.fetchMeBio(),
          api.fetchMeProfile(),
        ]);

        const userData = userDataResponse.bio || {};
        const bioData = bioDataResponse.bio || bioDataResponse;
        const profileData = profileDataResponse.profile || profileDataResponse

        setProfile({
          ...bioData,
          username: userData.username,
          profileImageName: userData.profileImageName,
          aboutMeText: profileData.userDescription,
          email: profileData.email,
        });

      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    }

    fetchProfile();
  }, [authToken]);

  return { profile, loading, error };
}