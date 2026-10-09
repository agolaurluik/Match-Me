import { useState, useEffect } from 'react';
import api from '../api/api';
import useOptions from './useOptions';

const findNameById = (list, id) => list?.find(item => item.id === id)?.name || 'Fetch error';

export function useUserProfiles({ userIds = null, fetchFromRecommendations = true, refreshKey = 0 } = {}) {

  const { purposes, genders, nationalities, interests, personalities, loading: optionsLoading } = useOptions();

  const getPurposeName = (id) => findNameById(purposes, id);
  const getGenderName = (id) => findNameById(genders, id);
  const getNationalityName = (id) => findNameById(nationalities, id);
  const getInterestNames = (ids = []) =>
    Array.isArray(ids)
      ? ids.map(id => findNameById(interests, id))
      : [];
  const getPersonalityNames = (ids = []) =>
    Array.isArray(ids)
      ? ids.map(id => findNameById(personalities, id))
      : [];

    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        if (optionsLoading) return;

        let isMounted = true;

        const fetchData = async () => {
            setLoading(true);
            setError(null);

            try{
              let ids = userIds;


              if (!ids && !fetchFromRecommendations) {
                if (isMounted) {
                  setUsers([]);
                  setLoading(false);
                }
                return;
              }

                if (fetchFromRecommendations && !userIds) {
                  const { recommendations } = await api.fetchRecommendations();
                  ids = recommendations;
                }

                
                if (!Array.isArray(ids) || ids.length === 0) {
                  if (isMounted) {
                    setUsers([]);
                    setLoading(false);
                  }
                  return;
                }

          const usersData = await Promise.all(
            ids.map(async (id) => {
            try {

              const [userRes, bioRes, matchRes] = await Promise.all([
                api.fetchUserById(id),
                api.fetchUserBioById(id),
                api.fetchUsersMatchingInfoById(id),
              ]);

              const user = userRes.user;
              const bio = bioRes.profile;
              const matchingInfo = matchRes.matchInfo;

              // console.log("Matching info for user ID:", { user, bio, matchingInfo });

              function calculateAge(birthDateStringOrDate) {
                if (!birthDateStringOrDate) return null;

                const birthDate = new Date(birthDateStringOrDate);
                if (isNaN(birthDate)) return null;

                const today = new Date();
                let age = today.getFullYear() - birthDate.getFullYear();

                const monthDiff = today.getMonth() - birthDate.getMonth();
                const dayDiff = today.getDate() - birthDate.getDate();

                if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
                  age--;
                }

                return age;
              }

              return {
                id: user.id,
                username: user.username,
                profilePicture: user.profileImageName  || 'no picture',
                bio: {
                  gender: getGenderName(bio.genderId),
                  age: calculateAge(bio.birthDate),
                  purpose: getPurposeName(bio.purposeId),
                  nationality: getNationalityName(bio.nationalityId),
                  interests: getInterestNames(bio.interestIds),
                  personalities: getPersonalityNames(bio.personalityIds),
                  sharedInterests: matchingInfo.sharedInterestsSet,
                  sharedPersonalities: matchingInfo.sharedPersonalitiesSet,
                  distance: matchingInfo.distance,
                  score: matchingInfo.matchPercentageRounded
                }
              };
            } catch (err) {
              console.error(`Error fetching data for user ID ${id}:`, err);
              return null; 
            }
          })
        );

        const filteredUsers = usersData.filter(Boolean); // Remove null entries?

        if (isMounted) {
          setUsers(filteredUsers );
          setLoading(false);
        }
      } catch (err) {
        if (isMounted) {
          setError(err);
          setLoading(false);
        }
      }
    }

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [optionsLoading, userIds?.join(','), fetchFromRecommendations, refreshKey]);

  return { users, loading, error};
}