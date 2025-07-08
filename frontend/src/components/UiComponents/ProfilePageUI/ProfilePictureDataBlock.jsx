import React from "react";
import './ProfilePictureDataBlock.css'


const ProfilePictureDataBlock = ({ imageUrl, altText = 'User profile picture', size = 100, user = "Error" }) => {
    return (
        <div className="profile-pic-data-block">
            <div className="profile-username-wrapper">
                <h2 className="profile-username">{user}</h2>
            </div>

            <div className="profile-image-wrapper">
                {imageUrl ? (
                    <img
                        src={imageUrl}
                        alt={altText}
                        className="profile-picture"
                        style={{ width: size, height: size }}
                    />
                ) : (
                    <span role="img" aria-label="placeholder" className="profile-picture-placeholder">
                        👤
                    </span>
                )}
            </div>
        </div>
    );
};

export default ProfilePictureDataBlock;