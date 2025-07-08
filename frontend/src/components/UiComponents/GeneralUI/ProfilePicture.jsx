import React from "react";
import "./ProfilePicture.css";

const ProfilePicture = ({ imageUrl, altText = "User profile picture", size = 50 }) => {
  const sizeStyle = {
    width: size,
    height: size,
  };

  return (
    <div className="header-profile-picture" style={sizeStyle}>
      {imageUrl ? (
        <img src={imageUrl} alt={altText} />
      ) : (
        <span role="img" aria-label="placeholder">
          👤
        </span>
      )}
    </div>
  );
};

export default ProfilePicture;