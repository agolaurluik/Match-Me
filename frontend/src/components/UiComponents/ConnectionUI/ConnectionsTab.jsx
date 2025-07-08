import React, { useState, useEffect } from "react";
import "./ConnectionsTab.css";
import UserGrid from "./UsersGrid";
import {useUserProfiles} from '../../../hooks/useUserProfiles';



const ConnectionsTab = () => {

    const [refreshTrigger, setRefreshTrigger] = useState(0);
     const { users, loading, error } = useUserProfiles({ refreshKey: refreshTrigger });

     
    // const { users: fetchedUsers, loading, error } = useUserProfiles({ refreshKey: refreshTrigger });
    // const [users, setUsers] = useState([]); //not used for removing anymore
    
    // useEffect(() => {
    //   setUsers(fetchedUsers);
    // }, [fetchedUsers]);

    const removeUserFromList = (userId) => {
      // setUsers(prevUsers => prevUsers.filter(user => user.id !== userId));
      setRefreshTrigger(prev => prev + 1); //originally was removing users from list of 9 one by one but now full refreshing when accepted of declined user.
    };

 

    if (loading) return <div className='connections-tab-container-message'><h2>Loading recommendations...</h2></div>;
    if (error) return <div className='connections-tab-container-message'><h2>Failed to load recommendations</h2></div>;

        if (!users || users.length === 0) {
      return (
        <div className='connections-tab-container-message'>
          <h2>No incoming connections</h2>
        </div>
      );
    }

  return (
    <UserGrid users={users}
    classPrefix="connections-tab"
    onUserRemoved={removeUserFromList} />
  );
};

export default ConnectionsTab;