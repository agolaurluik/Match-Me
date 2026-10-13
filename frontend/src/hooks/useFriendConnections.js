
import { useEffect, useState, useCallback } from 'react';
import api from '../api/api';
import useOptions from './useOptions';
import { useStomp } from '../components/UiComponents/ConnectionUI/StompProvider';

const findNameById = (list, id) =>
  list?.find(item => item.id === id)?.name || 'Unknown';

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
  const getInterestNames = (ids) =>
    ids?.map(id => findNameById(interests, id)) || [];
  const getPersonalityNames = (ids) =>
    ids?.map(id => findNameById(personalities, id)) || [];

  const removeChatConnection = (userId) => {
    setConnections(prev =>
      prev.filter(conn => conn.friendId !== userId)
    );

    setUnreadCounts(prev => {
      const updated = { ...prev };
      delete updated[userId];
      return updated;
    });
  };

  const updateConnectionStatus = useCallback((userId, updatedFields) => {
    setConnections(prev =>
      prev.map(conn =>
        conn.friendId === userId
          ? { ...conn, ...updatedFields }
          : conn
      )
    );
  }, []);

  const refreshConnectionStatus = useCallback(async (friendId) => {
    try {
      const updatedConnectionData =
        await api.fetchConnectionDataByUserId(friendId);

      updateConnectionStatus(friendId, {
        senderStatus: updatedConnectionData.senderStatus,
        receiverStatus: updatedConnectionData.receiverStatus,
        connectionId: updatedConnectionData.connectionId,
      });
    } catch (err) {
      console.error('Failed to refresh connection status:', err);
    }
  }, [updateConnectionStatus]);

  useEffect(() => {
    setRefreshConnectionStatusFn(() => refreshConnectionStatus);
  }, [refreshConnectionStatus, setRefreshConnectionStatusFn]);

  useEffect(() => {
    if (optionsLoading || !viewerId) return;

    let cancelled = false;

    const fetchConnections = async () => {
      setLoading(true);

      try {
        // Assumes this endpoint now returns full connection objects,
        // not just friend IDs.
        const connectionData = await api.fetchUserConnections();

        const enrichedConnections = await Promise.all(
          connectionData.map(async (conn) => {
            const friendId =
              Number(conn.senderId) === Number(viewerId)
                ? conn.receiverId
                : conn.senderId;

            try {
              const [userData, userBio, userProfile, matchInfo] =
                await Promise.all([
                  api.fetchUserById(friendId),
                  api.fetchUserBioById(friendId),
                  api.fetchUserProfileById(friendId).catch(err => {
                    console.error(
                      `Failed to fetch profile for user ${friendId}:`,
                      err
                    );
                    return null;
                  }),
                  api.fetchUsersMatchingInfoById(friendId),
                ]);

              const bio = userBio.profile;
              const matchingInfo = matchInfo.matchInfo;

              function calculateAge(birthDateStringOrDate) {
                if (!birthDateStringOrDate) return null;

                const birthDate = new Date(birthDateStringOrDate);
                if (Number.isNaN(birthDate.getTime())) return null;

                const today = new Date();
                let age = today.getFullYear() - birthDate.getFullYear();

                const monthDiff = today.getMonth() - birthDate.getMonth();
                const dayDiff = today.getDate() - birthDate.getDate();

                if (
                  monthDiff < 0 ||
                  (monthDiff === 0 && dayDiff < 0)
                ) {
                  age--;
                }

                return age;
              }

              return {
                ...userData.user,
                ...conn,

                // Keep the user ID and connection ID separate.
                connectionId: conn.connectionId,
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
                  sharedInterests: matchingInfo.sharedInterestsSet,
                  sharedPersonalities: matchingInfo.sharedPersonalitiesSet,
                  distance: matchingInfo.distance,
                  score: matchingInfo.matchPercentage ?? null,
                },
              };
            } catch (err) {
              console.error(
                `Failed to fetch full data for user ${friendId}`,
                err
              );

              // Preserve the connection information even if enrichment fails.
              return {
                ...conn,
                connectionId: conn.connectionId,
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
            if (!conn.connectionId) {
              console.warn(
                `Skipping unread messages for friend ${conn.friendId}: missing connection ID`,
                conn
              );
              return;
            }

            try {
              const history = await api.fetchChatHistory(
                conn.connectionId,
                0,
                50
              );

              const unread = history.content.filter(
                msg =>
                  Number(msg.receiverId ?? msg.receiver) === Number(viewerId) &&
                  msg.status === 'SENT'
              );

              if (unread.length > 0) {
                unreadCountsTemp[conn.friendId] = unread.length;
              }
            } catch (err) {
              console.error(
                `Failed to get unread messages for friend ${conn.friendId}, connection ${conn.connectionId}:`,
                err
              );
            }
          })
        );

        if (cancelled) return;

        setConnections(enrichedConnections);
        setUnreadCounts(unreadCountsTemp);
      } catch (err) {
        if (!cancelled) {
          console.error('Error loading connections:', err);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchConnections();

    return () => {
      cancelled = true;
    };
  }, [viewerId, optionsLoading]);

  return {
    connections,
    unreadCounts,
    setConnections,
    setUnreadCounts,
    loading,
    removeChatConnection,
    updateConnectionStatus,
  };
};

export default useFriendConnections;
