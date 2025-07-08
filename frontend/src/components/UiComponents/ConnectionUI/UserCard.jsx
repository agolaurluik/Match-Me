import React from 'react';

const UserCard = ({name, profilePicture}) => {
    return (
        <button className='user-card'>
            <div className='user-card-image'>
                {profilePicture ? (
                    <img
                        src={profilePicture}
                        alt={`${name}'s profile`}
                        className="user-car-img"
                    />

                ) : (
                    <div className="user-card__placeholder">
                    👤
                    </div>
                )}
            </div>
            <h3 className="user-card__name">{name}</h3>
        </button>
    )
}


export default UserCard;