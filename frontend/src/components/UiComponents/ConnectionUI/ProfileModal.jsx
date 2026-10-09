import React from "react";
import "./ProfileModal.css";
import { useSecureImage } from '../../../hooks/useSecureImage';
import api from '../../../api/api';
import { useStomp } from './StompProvider';




const ProfileModal = ({ user, onClose, classPrefix, modalPrefix, onUserRemoved, updateConnectionStatus }) => {
    if (!user) return null;

    const { sendConnectionStatusUpdate } = useStomp();
    const profileSecureImageUrl = useSecureImage(user.profilePicture);

    const isIncomingTab = modalPrefix === "incoming-connections-tab";
    const isPendingTab = modalPrefix === "pending-connections-tab";
    const isChatTab = classPrefix === "chat-tab"; //also includes about-me for user profile in chat
    const isBlockedByMe = user.userIsReceiver
        ? user.receiverStatus === "BLOCKED"
        : user.senderStatus === "BLOCKED";


    const isSender = !user.userIsReceiver;
    const myStatus = isSender ? user.senderStatus : user.receiverStatus;
    const theirStatus = isSender ? user.receiverStatus : user.senderStatus;

    // True if one side blocked and the other rejected (show only Delete)
    const isDeletedConnection = isChatTab && (
        (myStatus === "BLOCKED" && theirStatus === "REJECTED") ||
        (myStatus === "REJECTED" && theirStatus === "BLOCKED")
    );

    // True if connection is active (both accepted or neither rejected/blocked)
    const acceptedStatuses = ["ACCEPTED", "ACTIVE"];

    const bothAccepted = isChatTab &&
        acceptedStatuses.includes(myStatus) &&
        acceptedStatuses.includes(theirStatus);


    const handleMatch = async () => {
        try {
            if (isIncomingTab) {
                await api.acceptConnection(user.id);
                // console.log("Accepted connection from", user.username);
            } else {
                await api.createConnection(user.id);
                // console.log("Matched with", user.username);
            }
            onUserRemoved?.(user.friendId ?? user.id);
        } catch (error) {
            console.error("Error accepting(matching) connection:", error.message);
        } finally {
            onClose();
        }
    };

    const handleReject = async () => {
        try {
            if (isPendingTab) {
                await api.deleteConnection(user.id);
                // console.log("Canceled pending connection to ", user.username);
                onUserRemoved?.(user.id);
            } else if (isIncomingTab) {
                await api.rejectConnection(user.id);
                // console.log("Rejected incoming connection from ", user.username);
                onUserRemoved?.(user.id);
                sendConnectionStatusUpdate(user.myId, user.id);
            } else if (isChatTab) {
                await api.rejectConnection(user.id);
                // console.log("Deleted connection with ", user.username);
                onUserRemoved?.(user.id);
                sendConnectionStatusUpdate(user.myId, user.id);
            } else {
                await api.createConnection(user.id);
                //  console.log("Created reccommended user ", user.username);
                await api.rejectConnection(user.id);
                // console.log("Blocked reccommended user ", user.username);
                onUserRemoved?.(user.friendId ?? user.id);
            }

        } catch (error) {
            console.error("Error rejecting(blocking) connection:", error.message);
        } finally {
            onClose();
        }
    };

    const handleBlock = async () => {
        try {
            if (isChatTab && !isBlockedByMe) {
                await api.blockConnection(user.id);
                // console.log("Blocked user connection with ", user.username);
                sendConnectionStatusUpdate(user.myId, user.id);

                const statusKey = user.userIsReceiver ? 'receiverStatus' : 'senderStatus';
                updateConnectionStatus?.(user.id, { [statusKey]: 'BLOCKED' });
            } else {
                await api.unblockConnection(user.id);
                // console.log("Unblocked user connection with ", user.username);
                sendConnectionStatusUpdate(user.myId, user.id);

                const statusKey = user.userIsReceiver ? 'receiverStatus' : 'senderStatus';
                updateConnectionStatus?.(user.id, { [statusKey]: 'ACCEPTED' }); // or whatever the fallback should be
            }
        } catch (error) {
            console.error("Error blocking/unblocking connection:", error.message);
        } finally {
            onClose();
        }
    };

    const handleCancel = async () => {
        try {
            await api.deleteConnection(user.id);
            // console.log("Canceled/deleted user connection with ", user.id);
            onUserRemoved?.(user.id);
        } catch (error) {
            console.error("Error canceling pending match:", error.message);
        } finally {
            onClose();
        }
    };



    //connections tab profile modal is used on all 4 tabs now. So tabs should be changed to page for clarity
    return (
        <div className="connections-tab-profile-modal-overlay" onClick={onClose}>
            <div className="connections-tab-profile-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="connections-tab-profile-modal-close" onClick={onClose}>×</button>
                <div className="connections-tab-profile-modal-image-container">
                    <img
                        src={profileSecureImageUrl}
                        alt={user.name}
                        className="connections-tab-profile-modal-image"
                    />

                    <div className="connections-tab-profile-header-info">
                        <h2>{user.username}</h2>

                        {isChatTab && (
                            <div className="connections-tab-profile-about-me">
                                <h3>About Me</h3>
                                <p>{user.aboutMe || "No description provided."}</p>
                            </div>
                        )}
                    </div>
                </div>

                <section className="profile-modal-section">
                    <h3>Basic information</h3>

                    <div className="profile-modal-info-grid">
                        <div className="profile-modal-info-item">
                            <span>Age</span>
                            <strong>{user.bio.age}</strong>
                        </div>

                        <div className="profile-modal-info-item">
                            <span>Gender</span>
                            <strong>{user.bio.gender}</strong>
                        </div>

                        <div className="profile-modal-info-item">
                            <span>Purpose</span>
                            <strong>{user.bio.purpose}</strong>
                        </div>

                        <div className="profile-modal-info-item">
                            <span>Nationality</span>
                            <strong>{user.bio.nationality}</strong>
                        </div>
                    </div>
                </section>

                <section className="profile-modal-section">
                    <h3>Interests & personality</h3>

                    <div className="profile-modal-tag-group">
                        <div>
                            <span className="profile-modal-label">Interests</span>

                            <div className="profile-modal-tags">
                                {user.bio.interests.map((interest, index) => {
                                    const isShared = user.bio.sharedInterests?.includes(interest);

                                    return (
                                        <span
                                            key={index}
                                            className={`profile-modal-tag ${isShared ? 'shared' : ''}`}
                                        >
                                            {interest}
                                        </span>
                                    );
                                })}
                            </div>
                        </div>

                        <div>
                            <span className="profile-modal-label">Personality</span>

                            <div className="profile-modal-tags">
                                {user.bio.personalities.map((personality, index) => {
                                    const isShared =
                                        user.bio.sharedPersonalities?.includes(personality);

                                    return (
                                        <span
                                            key={index}
                                            className={`profile-modal-tag ${isShared ? 'shared' : ''}`}
                                        >
                                            {personality}
                                        </span>
                                    );
                                })}
                            </div>
                        </div>
                    </div>
                </section>

                <section className="profile-modal-section profile-modal-matching">
                    <h3>Matching</h3>

                    <div className="profile-modal-match-grid">
                        <div className="profile-modal-match-item">
                            <span>Distance</span>
                            <strong>{user.bio.distance} km</strong>
                        </div>

                        <div className="profile-modal-match-item">
                            <span>Matching score</span>
                            <strong>
                                {"⭐".repeat(Math.ceil(user.bio.score / 20))}{" "}
                                {user.bio.score}%
                            </strong>
                        </div>
                    </div>
                </section>

                <div className="connections-tab-profile-modal-actions">
                    {!isPendingTab && !isChatTab && (
                        <button className="connections-tab-match-button" onClick={handleMatch}>
                            Match ✅
                        </button>
                    )}


                    {isChatTab && isDeletedConnection && (
                        <button
                            className="connections-tab-delete-button"
                            onClick={handleReject}
                        >
                            Delete 🗑️
                        </button>
                    )}

                    {isChatTab && bothAccepted && (
                        <>
                            <button className="connections-tab-reject-button" onClick={handleBlock}>
                                {isBlockedByMe ? "Unblock 🔓" : "Block ❌"}
                            </button>
                            <button
                                className="connections-tab-delete-button"
                                onClick={handleReject}
                            >
                                Delete 🗑️
                            </button>
                        </>
                    )}


                    {isChatTab && !isDeletedConnection && !bothAccepted && (
                        <button className="connections-tab-reject-button" onClick={handleBlock}>
                            {isBlockedByMe ? "Unblock 🔓" : "Block ❌"}
                        </button>
                    )}

                    {isPendingTab && (
                        <button className="connections-tab-reject-button" onClick={handleCancel}>
                            Cancel ❌
                        </button>
                    )}

                    {!isPendingTab && !isChatTab && (
                        <button className="connections-tab-reject-button" onClick={handleReject}>
                            Reject ❌
                        </button>
                    )}

                </div>
            </div>
        </div>
    )
}

export default ProfileModal;