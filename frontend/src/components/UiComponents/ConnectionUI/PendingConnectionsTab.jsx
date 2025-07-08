import React, { useEffect, useState, useRef } from 'react';
import './PendingConnectionsTab.css';
import UserGrid from './UsersGrid';
import {useUserProfiles} from '../../../hooks/useUserProfiles';

import api from '../../../api/api';

const PendingConnectionsTab = () => {


    const [outgoingIds, setOutgoingIds] = useState(null);
    const [users, setUsers] = useState([]);
 
  useEffect(() => {
    async function fetchOutgoing() {
        try {
        const data = await api.fetchOutgoingPendingConnections();
        const ids = data.map(conn => conn.receiver);
        
        setOutgoingIds(ids);
      } catch (err) {
        console.error("Failed to fetch outgoing connections", err);
      }
    }

    fetchOutgoing();
  }, []);

  const {
    users:  pendingUsersList,
            loading,
              error
  } = useUserProfiles({ userIds: outgoingIds, fetchFromRecommendations: false });


useEffect(() => {
  if (pendingUsersList) {
    setUsers(pendingUsersList);
  }
}, [pendingUsersList]);

  const removeUserFromList = (userId) => {
    setUsers(prev => prev.filter(user => user.id !== userId));
  };



    if (loading) return <div className='pending-connections-tab-container'>Loading pending connections...</div>;
    if (error) return <div className='pending-connections-tab-container'>Failed to load pending connections</div>;
    if (!users || users.length === 0) {
    return (
      <div className='pending-connections-tab-container-message'>
        <h2>No pending connections</h2>
      </div>
    );
  }

  return (
    <UserGrid
      users={users}
      classPrefix="pending-connections-tab"
      onUserRemoved={removeUserFromList}
    />
  );
};

export default PendingConnectionsTab;