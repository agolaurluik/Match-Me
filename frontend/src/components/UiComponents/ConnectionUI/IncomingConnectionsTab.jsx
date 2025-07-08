import React, { useEffect, useState } from 'react';
import './IncomingConnectionsTab.css';
import UserGrid from './UsersGrid';
import {useUserProfiles} from '../../../hooks/useUserProfiles';
import api from '../../../api/api';

const IncomingConnectionsTab = () => {

  const [incomingIds, setIncomingIds] = useState(null);
  const [users, setUsers] = useState([]);

 
    useEffect(() => {
      async function fetchIncoming() {
        try {
          const data = await api.fetchIncomingPendingConnections();
          const ids = data.map(conn => conn.sender);
          setIncomingIds(ids);
        } catch (err) {
          console.error("Failed to fetch incoming connections", err);
        }
      }

      fetchIncoming();
    }, []);

      const {
      users: incomingUsersList,
      loading,
      error
    } = useUserProfiles({ userIds: incomingIds, fetchFromRecommendations: false });

        useEffect(() => {
        if (incomingUsersList) {
          setUsers(incomingUsersList);
        }
      }, [incomingUsersList]);
    
      const removeUserFromList = (userId) => {
        setUsers(prev => prev.filter(user => user.id !== userId));
      };
    

    if (loading) return <div className='incoming-connections-tab-container'>Loading incoming connections...</div>;
    if (error) return <div className='incoming-connections-tab-container'>Failed to load incoming connections</div>;

    if (!incomingUsersList || incomingUsersList.length === 0) {
      return (
        <div className='incoming-connections-tab-container-message'>
          <h2>No incoming connections</h2>
        </div>
      );
    }

  return (
    <UserGrid
      users={users}
      classPrefix="incoming-connections-tab"
      onUserRemoved={removeUserFromList}
    />
  );
};

export default IncomingConnectionsTab;