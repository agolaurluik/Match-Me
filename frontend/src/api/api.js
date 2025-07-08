const API_BASE_URL = 'http://localhost:8080/api';

function getToken() {
    return localStorage.getItem('Authorization');
}

async function fetchJSON(url) {
    const response = await fetch(url, {
        headers: {
            'Authorization': getToken()
        }
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || error.message || 'Request failed');
    }
    return response.json();
}

async function postJSON(url, data) {
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getToken()
        },
        body: JSON.stringify(data),

    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || error.message || 'Request failed');
    }
    return response.json();
}

async function putFormData(url, formData) {
    const response = await fetch(url, {
        method: 'PUT',
        headers: {
            'Authorization': getToken()
        },
        body: formData,
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || error.message || 'Request failed');
    }
    return response.json();
}

async function putData(url, data) {
    const response = await fetch(url, {
        method: 'PUT',
        headers: {
            'Authorization': getToken(),
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || error.message || 'Request failed');
    }
    return response.json();
}

async function deleteData(url) {
    const response = await fetch(url, {
        method: 'DELETE',
        headers: {
            'Authorization': getToken()
        }
    });

    if (!response.ok) {
        let errorMessage = 'Delete request failed';
        try {
            const error = await response.json();
            errorMessage = error.error || error.message || errorMessage;
        } catch {
            // response body empty
        }
        throw new Error(errorMessage);
    }

    if (response.status === 204 || response.headers.get("Content-Length") === "0") {
        return null;
    }

    try {
        return await response.json();
    } catch {
        return null; 
    }
}

async function patchJSON(url, data) {
    const response = await fetch(url, {
        method: 'PATCH',
        headers: {
            'Authorization': getToken(),
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        let errorMessage = 'Request failed';
        try {
            const error = await response.json();
            errorMessage = error.error || error.message || errorMessage;
        } catch {
        }
        throw new Error(errorMessage);
    }

    const contentLength = response.headers.get('Content-Length');
    if (response.status === 204 || contentLength === '0') {
        return null;
    }

    try {
        return await response.json();
    } catch {
        return null;
    }
}

export async function loadImageWithToken(url) {
    const response = await fetch(url, {
        headers: {
            "Authorization": getToken()
        }
    });

    if (!response.ok) {
        throw new Error("Failed to fetch image");
    }

    const blob = await response.blob();
    return URL.createObjectURL(blob);
}

const api = {
    registerUser: (userData) => postJSON(`${API_BASE_URL}/auth/register`, userData),
    loginUser: (credentials) => postJSON(`${API_BASE_URL}/auth/login`, credentials),

    updateProfile: (data) => putData(`${API_BASE_URL}/users/me/profile`, data),
    patchProfile: (data) => patchJSON(`${API_BASE_URL}/users/me/profile`, data),
    createProfile: (data) => postJSON(`${API_BASE_URL}/users/me/profile`, data),

    fetchPersonalities: () => fetchJSON(`${API_BASE_URL}/matching/getAllPersonalities`),
    fetchNationalities: () => fetchJSON(`${API_BASE_URL}/matching/getAllNationalities`),
    fetchPurposes: () => fetchJSON(`${API_BASE_URL}/matching/getAllPurposes`),
    fetchInterests: () => fetchJSON(`${API_BASE_URL}/matching/getAllInterests`),
    fetchGenders: () => fetchJSON(`${API_BASE_URL}/matching/getAllGenders`),
    fetchLocations: () => fetchJSON(`${API_BASE_URL}/matching/getAllNamedLocations`),

    fetchRecommendations: () => fetchJSON(`${API_BASE_URL}/matching/recommendations`),
    fetchUsersMatchingInfoById: (userId) => fetchJSON(`${API_BASE_URL}/matching/getMatchInfoByID/${userId}`),

    fetchMatchedUsers: (data) => postJSON(`${API_BASE_URL}/matchedUsers`, data),

    fetchMatchingFilter: () => fetchJSON(`${API_BASE_URL}/matching/filter`),
    patchMatchingFilter: (data) => patchJSON(`${API_BASE_URL}/matching/updateMatchingFilter`, data),

    fetchUserById: (userId) => fetchJSON(`${API_BASE_URL}/users/${userId}`),
    fetchUserBioById: (userId) => fetchJSON(`${API_BASE_URL}/users/${userId}/bio`),
    fetchUserProfileById: (userId) => fetchJSON(`${API_BASE_URL}/users/${userId}/profile`),

    fetchMeById: () => fetchJSON(`${API_BASE_URL}/users/me`),
    fetchMeBio: () => fetchJSON(`${API_BASE_URL}/users/me/bio`),
    fetchMeProfile: () => fetchJSON(`${API_BASE_URL}/users/me/profile`),
    fetchMeLocation: () => fetchJSON(`${API_BASE_URL}/getLocation/me`),

    uploadProfilePicture: (formData) => putFormData(`${API_BASE_URL}/image/update`, formData),
    resetProfilePicture: () => deleteData(`${API_BASE_URL}/image/reset`),

    updateUserLocation: (data) => putData(`${API_BASE_URL}/users/me/profile/updateUserLocation`, data),
    createUserLocation: (data) => postJSON(`${API_BASE_URL}/users/me/profile/createUserLocation`, data),

    createConnection: (userId) => postJSON(`${API_BASE_URL}/connections/create/${userId}`),
    
    blockConnection: (userId) => postJSON(`${API_BASE_URL}/connections/${userId}/block`),
    unblockConnection: (userId) => postJSON(`${API_BASE_URL}/connections/${userId}/unblock`),

    acceptConnection: (userId) => postJSON(`${API_BASE_URL}/connections/${userId}/accept`),
    rejectConnection: (userId) => postJSON(`${API_BASE_URL}/connections/${userId}/reject`),
    deleteConnection: (userId) => deleteData(`${API_BASE_URL}/connections/${userId}/delete`),

    fetchOutgoingPendingConnections: () => fetchJSON(`${API_BASE_URL}/connections/outgoing`),
    fetchIncomingPendingConnections: () => fetchJSON(`${API_BASE_URL}/connections/incoming`),

    fetchFriendConnections: () => fetchJSON(`${API_BASE_URL}/connections/friend-liszt`),
    fetchUserConnections: () => fetchJSON(`${API_BASE_URL}/connections`),
    fetchConnectionDataByUserId: (userId) => fetchJSON(`${API_BASE_URL}/connections/${userId}`),


    fetchUserNameById: (userId) => fetchJSON(`${API_BASE_URL}/users/get-username/${userId}`),
    fetchChatHistory: (connectionId, page = 0, size = 20) =>
    fetchJSON(`${API_BASE_URL}/chat/history?connectionId=${connectionId}&page=${page}&size=${size}`), 
    setMessagesAsRead: (connectionId) => patchJSON(`${API_BASE_URL}/chat/read?connectionId=${connectionId}`)

};

export default api;