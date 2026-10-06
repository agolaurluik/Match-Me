import { useEffect, useState, useCallback } from 'react';
import api from '../api/api';
import useOptions from './useOptions';
import { useStomp } from '../components/UiComponents/ConnectionUI/StompProvider';

const findNameById = (list, id) => list?.find(item => item.id === id)?.name || 'Unknown';

const useFriendConnections = (viewerId) => {
  const [connections, setConnections] = useState([]);
  const [unreadCounts, setUnreadCounts] = useState({});
  const [loading, setLoading] = useState(true);


  const { setRefreshConnectionStatusFn } = useStomp();


  const {
    purposes,
    genders,
    nationalities,
    interests,
    personalities,
    loading: optionsLoading,
  } = useOptions();

  const getPurposeName = (id) => findNameById(purposes, id);
  const getGenderName = (id) => findNameById(genders, id);
  const getNationalityName = (id) => findNameById(nationalities, id);
  const getInterestNames = (ids) => ids?.map(id => findNameById(interests, id)) || [];
  const getPersonalityNames = (ids) => ids?.map(id => findNameById(personalities, id)) || [];

  const removeChatConnection = (userId) => {
    console.log("[Hook] removeChatConnection called with userId:", userId);
    setConnections((prev) => prev.filter((conn) => conn.friendId !== userId));
    setUnreadCounts((prev) => {
      const updated = { ...prev };
      delete updated[userId];
      return updated;
    });
  };

  const updateConnectionStatus = useCallback((userId, updatedFields) => { //Call back to avoid infinite loops, used when truly necessary.. (profile modal block)
    // console.log("updating id : ", userId, updatedFields);
    setConnections(prev =>
      prev.map(conn =>
        conn.friendId === userId ? { ...conn, ...updatedFields } : conn
      )
    );
  }, [setConnections]);


  const refreshConnectionStatus = useCallback(async (friendId) => { //(socket)
    // console.log("pump!:", friendId);
    try {
      const updatedConnectionData = await api.fetchConnectionDataByUserId(friendId);

      updateConnectionStatus(friendId, {
        senderStatus: updatedConnectionData.senderStatus,
        receiverStatus: updatedConnectionData.receiverStatus,
      });
    } catch (err) {
      console.error("Failed to refresh connection status:", err);
    }
  }, [updateConnectionStatus]);

  useEffect(() => {
    setRefreshConnectionStatusFn(() => refreshConnectionStatus);
  }, [refreshConnectionStatus]);


  useEffect(() => {
    if (optionsLoading || !viewerId) return;

    const fetchConnections = async () => {
      setLoading(true);

      try {
        const friendIds = await api.fetchUserConnections();

        const enrichedConnections = await Promise.all(
          friendIds.map(async (conn) => {
            const friendId = conn;

            try {
              const [userConnData, userData, userBio, userProfile, matchInfo] = await Promise.all([
                api.fetchConnectionDataByUserId(friendId),
                api.fetchUserById(friendId),
                api.fetchUserBioById(friendId),
                api.fetchUserProfileById(friendId).catch(err => {
                  console.error(`Failed to fetch profile for user ${friendId}:`, err);
                  return null;
                }),
                api.fetchUsersMatchingInfoById(friendId),
              ]);

              const bio = userBio.profile;

              function calculateAge(birthDateStringOrDate) {
                if (!birthDateStringOrDate) return null;
                const birthDate = new Date(birthDateStringOrDate);
                if (isNaN(birthDate)) return null;
                const today = new Date();
                let age = today.getFullYear() - birthDate.getFullYear();
                const monthDiff = today.getMonth() - birthDate.getMonth();
                const dayDiff = today.getDate() - birthDate.getDate();
                if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) age--;
                return age;
              }

              return {
                ...userConnData,
                ...userData.user,
                friendId,
                profilePicture: userData.user.profileImageName,
                aboutMe: userProfile?.profile?.userDescription || '',
                bio: {
                  gender: getGenderName(bio.genderId),
                  age: calculateAge(bio.birthDate),
                  purpose: getPurposeName(bio.purposeId),
                  nationality: getNationalityName(bio.nationalityId),
                  interests: getInterestNames(bio.interestIds),
                  personalities: getPersonalityNames(bio.personalityIds),
                  sharedInterests: matchInfo.matchInfo.sharedInterests,
                  sharedPersonalities: matchInfo.matchInfo.sharedPersonalities,
                  distance: matchInfo.matchInfo.distance,
                  score: matchInfo.matchInfo.score,
                },
              };
            } catch (err) {
              console.error(`Failed to fetch full data for ID ${friendId}`, err);
              return {
                friendId,
                profilePicture: null,
                bio: null,
                aboutMe: '',
              };
            }
          })
        );

        const unreadCountsTemp = {};
        await Promise.all(
          enrichedConnections.map(async (conn) => {
            try {
              const history = await api.fetchChatHistory(conn.connectionId, 0, 50);
              const unread = history.content.filter(
                (msg) => msg.receiver === viewerId && msg.status === 'SENT'
              );
              if (unread.length > 0) unreadCountsTemp[conn.friendId] = unread.length;
            } catch (err) {
              console.error('Failed to get unread messages: ', err);
            }
          })
        );


        setConnections(enrichedConnections);
        setUnreadCounts(unreadCountsTemp);
      } catch (err) {
        console.error('Error loading connections: ', err);
      } finally {
        setLoading(false);
      }
    };

    fetchConnections();
  }, [viewerId, optionsLoading]);

  return { connections, unreadCounts, setConnections, setUnreadCounts, loading, removeChatConnection, updateConnectionStatus };
};

export default useFriendConnections;
