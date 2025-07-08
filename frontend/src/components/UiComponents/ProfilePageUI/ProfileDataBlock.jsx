import React from "react";
import './ProfileDataBlock.css';


const ProfileDataBlock = ({
        header = "",
        email = '',
        items,
        displayMode = "list", // "list", "inline", "label-value"
        profileImage = null,   
}) => {

  const parsedItems = Array.isArray(items)
  ? items.filter(item => item && item.trim() !== '').slice(0, 5)
  : typeof items === "string" && items.trim() !== ''
  ?[items]
  :[];

  if (!header && parsedItems === 0 && !profileImage) {
    return null;
  }

const renderContent = () => {
  switch (displayMode) {
    case "label-value":
    case "inline":
      return (
        <div className={`profile-data-block-${displayMode}`}>
          {!profileImage && header && (
            <span className="profile-data-block-label">{header}:</span>
          )}
          <div className="profile-data-block-values">
            {parsedItems.map((item, idx) => (
              <div key={idx}>{item}</div>
            ))}
          </div>
        </div>
      );

    case "list":
    default:
      return (
        <>
          {!profileImage && header && (
            <div className="profile-data-block-header-box">
              <h4 className="profile-data-block-header">{header}</h4>
            </div>
          )}
          <ul className="profile-data-block-list">
            {parsedItems.map((item, index) => (
              <li key={index} className="profile-data-block-item">
                {item}
              </li>
            ))}
          </ul>
        </>
      );
  }
};

    return (
    <div className="profile-data-block">
      {profileImage && (
        <div className="profile-data-block-image-wrapper">
          {header && <div className="profile-data-block-header-inside-image">{header}</div>}
          {email}
          <img
            src={profileImage}
            alt={`${header || 'Profile'} picture`}
            className="profile-data-block-image"
          />
        </div>
      )}
      <div className="profile-data-block-content">
        {renderContent()}
      </div>
    </div>
  );
};


export default ProfileDataBlock;