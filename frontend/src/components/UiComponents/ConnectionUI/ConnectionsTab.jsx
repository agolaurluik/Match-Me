import React, { useState, useEffect } from "react";
import "./ConnectionsTab.css";
import UserGrid from "./UsersGrid";
import {useUserProfiles} from '../../../hooks/useUserProfiles';

const REFRESH_THRESHOLD = 3 // if 3 users are removed, refresh should happen.

const ConnectionsTab = () => {

    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const [removalCount, setRemovalCount] = useState(0);
    const [recommendedUsers, setRecommendedUsers] = useState([]);

    const { users, loading, error } = useUserProfiles({ refreshKey: refreshTrigger });

    useEffect(() => {
      if (users) {
        setRecommendedUsers(users);
      }
    }, [users]);

  const handleUserRemoved = (userId) => {
    setRecommendedUsers(prev => prev.filter(user => user.id !== userId));

    setRemovalCount(prev => {
      const newCount = prev + 1;

      if (newCount >= REFRESH_THRESHOLD) {
        setRefreshTrigger(prevKey => prevKey + 1);
        return 0;
      }

      return newCount;
    });
  };

 

    if (loading) return <div className='connections-tab-container-message'><h2>Loading recommendations...</h2></div>;
    if (error) return <div className='connections-tab-container-message'><h2>Failed to load recommendations</h2></div>;

        if (!recommendedUsers || recommendedUsers.length === 0) {
      return (
        <div className='connections-tab-container-message'>
          <h2>No Recommendations Available</h2>
        </div>
      );
    }

  return (
    <UserGrid users={recommendedUsers}
    classPrefix="connections-tab"
    onUserRemoved={handleUserRemoved} />
  );
};

export default ConnectionsTab;