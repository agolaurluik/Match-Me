import api from '../api/api';

export async function handleProfileSubmit({
  originalData,
  idName,
  gender,
  dob,
  nationality,
  purpose,
  interests,
  personalities,
  location,
  locationId,
  profilePic,
  namedLocationId,
  aboutMeText,
  navigate,
}) {

  try {
  const isNewProfile = originalData === null;
  const changedFields = {};


  const payload = {
    username: idName || null,
    birthDate: dob || null,
    genderId: gender?.id ?? null,
    nationalityId: nationality?.id ?? null,
    purposeId: purpose?.id ?? null,
    locationId: location?.id ?? null,
    profileImageName: null,  // to be set after upload if needed
    personalityIds: personalities.map(p => p.id),
    interestIds: interests.map(i => i.id),
    userDescription: aboutMeText || null,
  };
// console.log(payload)

  const locationPayload =
  location?.point
    ? location
    : (location?.id ? { locationId: location.id } : null);


// console.log("Location payload",locationPayload)


  if (isNewProfile || idName !== originalData.username) changedFields.username = payload.username;
  if (isNewProfile || dob !== originalData.birthDate) changedFields.birthDate = payload.birthDate;
  if (isNewProfile || (gender?.id ?? null) !== (originalData.genderId ?? null)) changedFields.genderId = payload.genderId;
  if (isNewProfile || (nationality?.id ?? null) !== (originalData.nationalityId ?? null)) changedFields.nationalityId = payload.nationalityId;
  if (isNewProfile || (purpose?.id ?? null) !== (originalData.purposeId ?? null)) changedFields.purposeId = payload.purposeId;
  if (isNewProfile || aboutMeText !== originalData.userDescription) changedFields.userDescription = payload.userDescription;
  const locationChanged = isNewProfile || (location?.id ?? null) !== (originalData.locationId ?? null);


  // Compare arrays of IDs as strings
  const originalPersonalityIds = originalData?.personalityIds || [];
  const originalInterestIds = originalData?.interestIds || [];

  if (
    isNewProfile ||
    JSON.stringify(payload.personalityIds) !== JSON.stringify(originalPersonalityIds)
  ) {
    changedFields.personalityIds = payload.personalityIds;
  }

  if (
    isNewProfile ||
    JSON.stringify(payload.interestIds) !== JSON.stringify(originalInterestIds)
  ) {
    changedFields.interestIds = payload.interestIds;
  }
  //handle time conversion to ms
  if (changedFields.birthDate) {
  changedFields.birthDate = (new Date(changedFields.birthDate)).toISOString();
  }

  // Handle profile picture upload
  let uploadedPicUrl = profilePic;
  // console.log(uploadedPicUrl)
  if (profilePic instanceof File) {
    const formData = new FormData();
    formData.append('file', profilePic);
      // console.log(formData)
    try {
      const result = await api.uploadProfilePicture(formData);
        // console.log(result)
      uploadedPicUrl = result.imageUrl;
      changedFields.profileImageURL = uploadedPicUrl;
    } catch (err) {
      console.error("Profile picture upload failed:", err.message);
    }
  } else if (profilePic && typeof profilePic === 'string') {
    // if profilePic is already a URL string
    changedFields.profileImageURL = profilePic;
  } else if (profilePic === null) {
    changedFields.profileImageURL = null;
  }

// console.log("Final profile payload:", changedFields);

if (isNewProfile) {
    await api.createProfile(changedFields);
      // console.log("Profile created!");
  } else if (Object.keys(changedFields).length === 0 && !locationChanged) {
      // console.log("No changes to save.");
  } else {
    if (Object.keys(changedFields).length > 0) {
        await api.patchProfile(changedFields);
        // console.log("Profile updated.");
    }
  }

  // console.log("Final location payload:", locationPayload);
  if (locationPayload) {
    if (namedLocationId || locationId) {
      await api.updateUserLocation(locationPayload);
    } else {
      await api.createUserLocation(locationPayload);
    }
  } else {
    // console.log("Skipping location submission – no valid location payload.");
  }

  navigate('/profile');

  } catch (err) {
    console.error("Profile submission failed:", err);
  }
}