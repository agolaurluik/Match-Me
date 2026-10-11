
import { useState, useEffect } from 'react';
import api from '../api/api';
import useOptions from './useOptions';

const findNameById = (list, id) =>
  list?.find(item => String(item.id) === String(id))?.name || 'Fetch error';

const getInterestNames = (ids = [], options = []) =>
  Array.isArray(ids)
    ? ids.map(id => findNameById(options, id)).filter(name => name !== 'Fetch error')
    : [];

const getPersonalityNames = (ids = [], options = []) =>
  Array.isArray(ids)
    ? ids.map(id => findNameById(options, id)).filter(name => name !== 'Fetch error')
    : [];

export function useUserProfiles({
  userIds = null,
  fetchFromRecommendations = true,
  connectionDirection = null,
  refreshKey = 0
} = {}) {
  const {
    purposes,
    genders,
    nationalities,
    interests,
    personalities,
    loading: optionsLoading
  } = useOptions();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (optionsLoading) return;

    let isMounted = true;

    const calculateAge = (birthDateStringOrDate) => {
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
    };

    const fetchData = async () => {
      setLoading(true);
      setError(null);

      try {
        let profiles;

        if (userIds) {
          profiles = userIds.map(item =>
            typeof item === 'object' && item !== null
              ? item
              : { id: item }
          );
        } else if (fetchFromRecommendations) {
          const response = await api.fetchRecommendations();
          profiles = response.recommendations;
        } else {
          profiles = [];
        }

        if (!Array.isArray(profiles) || profiles.length === 0) {
          if (isMounted) {
            setUsers([]);
            setLoading(false);
          }
          return;
        }

        const usersData = await Promise.all(
          profiles.map(async (profile) => {
            try {
              // Resolve the other user's ID.
              // Recommendations use id; connections use sender/receiver.
              const userId =
                profile.userId ??
                (connectionDirection === 'outgoing' ? profile.receiver : undefined) ??
                (connectionDirection === 'incoming' ? profile.sender : undefined) ??
                profile.id;

              if (userId == null) {
                console.error('Could not resolve user ID from profile:', profile);
                return null;
              }

              const [userRes, bioRes] = await Promise.all([
                api.fetchUserById(userId),
                api.fetchUserBioById(userId)
              ]);

              const user = userRes.user;
              const bio = bioRes.profile ?? bioRes.bio ?? bioRes;

              // These matching fields will be supplied by the backend
              // for incoming/outgoing connections.
              const sharedInterestIds = profile.sharedInterestsSet ?? [];
              const sharedPersonalityIds = profile.sharedPersonalitiesSet ?? [];

              return {
                // Keep the other user's ID for existing actions and filtering.
                id: user.id,

                // Preserve the connection ID separately for future use.
                connectionId: profile.sender != null || profile.receiver != null
                  ? profile.id
                  : null,

                username: user.username,

                profilePicture:
                  profile.profileImage ||
                  user.profileImageName ||
                  'no picture',

                bio: {
                  gender: genders.find(item =>
                    String(item.id) === String(bio.genderId)
                  )?.name || 'Fetch error',

                  age: calculateAge(bio.birthDate),

                  purpose: purposes.find(item =>
                    String(item.id) === String(bio.purposeId)
                  )?.name || 'Fetch error',

                  nationality: nationalities.find(item =>
                    String(item.id) === String(bio.nationalityId)
                  )?.name || 'Fetch error',

                  interests: getInterestNames(bio.interestIds, interests),
                  personalities: getPersonalityNames(bio.personalityIds, personalities),

                  // Preserve recommendation matching data.
                  // Pending connections use the same fields once the
                  // backend adds them to its response.
                  sharedInterests: Array.isArray(sharedInterestIds)
                    ? sharedInterestIds.map(item =>
                        typeof item === 'object' && item !== null
                          ? item.name
                          : interests.find(option =>
                              String(option.id) === String(item)
                            )?.name ?? item
                      )
                    : [],

                  sharedPersonalities: Array.isArray(sharedPersonalityIds)
                    ? sharedPersonalityIds.map(item =>
                        typeof item === 'object' && item !== null
                          ? item.name
                          : personalities.find(option =>
                              String(option.id) === String(item)
                            )?.name ?? item
                      )
                    : [],

                  distance: profile.distance ?? null,
                  score: profile.matchPercentage ?? null
                },

                // Preserve connection information for components that need it.
                sender: profile.sender,
                receiver: profile.receiver,
                senderStatus: profile.senderStatus,
                receiverStatus: profile.receiverStatus
              };
            } catch (err) {
              console.error(
                'Error fetching data for profile:',
                profile,
                err
              );
              return null;
            }
          })
        );

        if (isMounted) {
          setUsers(usersData.filter(Boolean));
          setLoading(false);
        }
      } catch (err) {
        console.error('Failed to fetch user profiles:', err);

        if (isMounted) {
          setError(err);
          setLoading(false);
        }
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [
    optionsLoading,
    userIds?.join(','),
    fetchFromRecommendations,
    connectionDirection,
    refreshKey,
    purposes,
    genders,
    nationalities,
    interests,
    personalities
  ]);

  return { users, loading, error };
}
