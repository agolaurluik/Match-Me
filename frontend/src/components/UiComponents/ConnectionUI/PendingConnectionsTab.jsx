import React, { useEffect, useState } from 'react';
import './ConnectionsTab.css';
import UserGrid from './UsersGrid';
import { useUserProfiles } from '../../../hooks/useUserProfiles';
import api from '../../../api/api';

const IncomingConnectionsTab = () => {
  const [incomingConnections, setIncomingConnections] = useState(null);
  const [users, setUsers] = useState([]);

  useEffect(() => {
    async function fetchIncoming() {
      try {
        const data = await api.fetchIncomingPendingConnections();
        setIncomingConnections(data);
      } catch (err) {
        console.error('Failed to fetch incoming connections', err);
        setIncomingConnections([]);
      }
    }

    fetchIncoming();
  }, []);

  const {
    users: incomingUsersList,
    loading,
    error
  } = useUserProfiles({
    userIds: incomingConnections,
    fetchFromRecommendations: false,
    connectionDirection: 'incoming'
  });

  useEffect(() => {
    setUsers(incomingUsersList);
  }, [incomingUsersList]);

  const removeUserFromList = (userId) => {
    setUsers(prev => prev.filter(user => user.id !== userId));
  };

  if (incomingConnections === null || loading) {
    return (
      <div className="connections-tab-container-message">
        Loading incoming connections...
      </div>
    );
  }

  if (error) {
    return (
      <div className="connections-tab-container-message">
        Failed to load incoming connections
      </div>
    );
  }

  if (users.length === 0) {
    return (
      <div className="connections-tab-container-message">
        <h2>No incoming connections</h2>
      </div>
    );
  }

  return (
    <UserGrid
      users={users}
      classPrefix="connections-tab"
      modalPrefix="incoming-connections-tab"
      onUserRemoved={removeUserFromList}
    />
  );
};

export default IncomingConnectionsTab;