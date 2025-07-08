import React, { useState } from "react";
import ProfileModal from "./ProfileModal";

const UserGrid = ({ users = [], classPrefix, onUserRemoved }) => {
  const [selectedUser, setSelectedUser] = useState(null);

  const [removingIds, setRemovingIds] = useState([]); //for removing id from list (before reloading)

const handleRemove = (userId) => {
  setRemovingIds(prev => [...prev, userId]);
  setTimeout(() => {
    onUserRemoved(userId);
    setRemovingIds(prev => prev.filter(id => id !== userId));
  }, 300); // match CSS animation duration
};

  return (
    <div className={`${classPrefix}-container`}>
      <div className={`${classPrefix}-grid-container`}>
        {users.map((user) => (
            <div
              key={user.id}
              className={`${classPrefix}-profile-card ${removingIds.includes(user.id) ? 'removing' : ''}`}
              onClick={() => setSelectedUser(user)}
            >
            <h2 className={`${classPrefix}-profile-name`}>{user.username}</h2>
            <p>{user.bio.gender}, {user.bio.age}</p>
            <p>Purpose: {user.bio.purpose}</p>
            <p>Nationality: {user.bio.nationality}</p>
            <p className={`${classPrefix}-match-info`}>
              🎯 {user.bio.sharedInterests} matching interests
            </p>
            <p className={`${classPrefix}-match-info`}>
              💡 {user.bio.sharedPersonalities} matching personalities
            </p>
            <p className={`${classPrefix}-match-info`}>
              🗺️ {user.bio.distance} km from your location.
            </p>
            <p className={`${classPrefix}-match-info`}>
              {"⭐".repeat(user.bio.score > 9 ? 3 : user.bio.score > 4 ? 2 : 1)} Matching score: {user.bio.score}.
            </p>
          </div>
        ))}
      </div>

      <ProfileModal 
        user={selectedUser}
        onClose={() => setSelectedUser(null)}
        classPrefix={classPrefix}
        onUserRemoved={handleRemove}
      />
    </div>
  );
};

export default UserGrid;