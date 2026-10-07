import React, { useState } from 'react';
import './Selector.css';

const Selector = ({
    type = 'personality',
    name = "No Data",
    options = [],
    selectedValues = [],
    onChange,
    maxSelection = 5,
    header = "'header' is empty",
    onResetPicture,
    z
}) => {
    const [locationMode, setLocationMode] = useState('geo');
    const [searchTerm, setSearchTerm] = useState('');
    const locationOptions = options || [];
    const [geoSuccess, setGeoSuccess] = useState(false);
    const [loadingLocation, setLoadingLocation] = useState(false);

    const handleSelect = (option) => {
        const optionName = option?.name || option;

        if (maxSelection === 1) {
            onChange(option);
            setSearchTerm('');
        } else {
            const alreadySelected = selectedValues.some(
                (val) => (val?.name || val) === optionName
            );

            if (!alreadySelected && selectedValues.length < maxSelection) {
                onChange([...selectedValues, option]);
                setSearchTerm('');
            }
        }
    };

    const handleRemove = (interest) => {
        const idToRemove = interest?.id || interest;
        if (maxSelection === 1) {
            onChange('');
        } else {
            onChange(
                selectedValues.filter(item => (item?.id || item) !== idToRemove)
            );
        }
    };

    const handleSingleSelect = (value) => {
        onChange(value);
    }

    const filteredOptions = options.filter(option => {
        const name = option?.name || option;
        return name.toLowerCase().includes(searchTerm.toLowerCase()) &&
            (Array.isArray(selectedValues)
                ? !selectedValues.some(selected => (selected?.id || selected) === (option?.id || option))
                : (selectedValues?.id || selectedValues) !== (option?.id || option));
    });

    const handleGetLocation = () => {
        if (!navigator.geolocation) {
            alert("Geolocation is not supported by your browser.");
            return;
        }

        setLoadingLocation(true);

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const payload = {
                    point: {
                        latitude: position.coords.latitude,
                        longitude: position.coords.longitude,
                    },
                    accuracy: position.coords.accuracy,
                    timestamp: new Date(position.timestamp).getTime(),
                };
                onChange(payload);
                setLoadingLocation(false);
                setGeoSuccess(true);
                console.log("Location payload:", payload);
            },
            (error) => {
                alert("Unable to retrieve your location.");
                console.error(error);
                setGeoSuccess(false);
            }
        );
    };

    return (
        <div className="selector-container">
            <h2 style={{ textAlign: 'center' }}>{header}</h2>

            {type === 'name' && (
                <div className="name-label">
                    <label htmlFor="name-input" className="name-heading">Your Name:</label>
                    <input
                        id="name-input"
                        className="name-selector-input"
                        type="text"
                        value={selectedValues || ''}
                        placeholder="Enter your name"
                        onChange={(e) => handleSingleSelect(e.target.value)}
                        maxLength={20}
                    />
                </div>
            )}


            {type === 'profilePicture' && (
                <div className="profile-picture-label">
                    {selectedValues ? (
                        <img
                            src={
                                selectedValues instanceof File
                                    ? URL.createObjectURL(selectedValues)
                                    : selectedValues
                            }
                            alt="Profile Preview"
                            className="profile-picture-preview"
                        />
                    ) : (
                        <div className="profile-picture-placeholder">No image selected</div>
                    )}

                    <div className="profile-picture-upload">
                        <label htmlFor="profile-picture-input" className="file-upload-button">
                            Choose Image
                        </label>

                        <input
                            id="profile-picture-input"
                            className="file-upload-input"
                            type="file"
                            accept="image/*"
                            onChange={(e) => {
                                const file = e.target.files[0];

                                if (file) {
                                    const maxSizeMB = 5;

                                    if (file.size / 1024 / 1024 > maxSizeMB) {
                                        alert(`File size should not exceed ${maxSizeMB} MB.`);
                                        e.target.value = null;
                                        return;
                                    }

                                    onChange(file);
                                }
                            }}
                        />
                    </div>

                    {onResetPicture && (
                        <button
                            className="selector-button reset-button"
                            type="button"
                            onClick={onResetPicture}
                        >Reset to Default Picture</button>
                    )}
                </div>
            )}

            {type === 'aboutMe' && (
                <div className="aboutme-label">
                    <label htmlFor="aboutme-input"></label>
                    <textarea
                        id="aboutme-input"
                        className="aboutme-textarea"
                        rows={6}
                        maxLength={500}
                        value={selectedValues || ''}
                        placeholder="Tell us something about yourself..."
                        onChange={(e) => handleSingleSelect(e.target.value)}
                    />
                </div>
            )}

            {type === 'dob' && (
                <div className="selector-date">
                    <label htmlFor="dob-input" className="dob-label">Date of Birth:</label>
                    <input
                        id="dob-input"
                        type="date"
                        className="dob-input"
                        value={selectedValues || ''}
                        onChange={(e) => handleSingleSelect(e.target.value)}
                        max={new Date().toISOString().split("T")[0]}
                    />
                </div>
            )}

            {type === 'gender' && (
                <div className="selector-options gender-grid">
                    {options.map((option, index) => (
                        <button
                            key={index}
                            className={`selector-button ${(selectedValues?.id || selectedValues) === option.id ? 'selected' : ''}`}
                            onClick={() => handleSingleSelect(option)}
                        >
                            {option.name}
                        </button>
                    ))}
                </div>
            )}

            {type === 'location' && (
                <div className="selector-location compact-location-selector">
                    <div className="location-toggle-buttons">
                        <button
                            className={`toggle-button ${locationMode === 'geo' ? 'active' : ''}`}
                            onClick={() => setLocationMode('geo')}
                        >   📍 Use My Location</button>
                        <button
                            className={`toggle-button ${locationMode === 'list' ? 'active' : ''}`}
                            onClick={() => setLocationMode('list')}
                        >    📂 Choose from List</button>
                    </div>

                    {locationMode === 'geo' && (
                        <>
                            <button className="location-button" onClick={handleGetLocation}>
                                Detect Location
                            </button>

                            {loadingLocation && <p className="loading-indicator">⏳ Fetching location...</p>}

                            {selectedValues?.latitude && (
                                <p className="location-coords">
                                    Lat: {selectedValues.latitude.toFixed(4)}, Lng: {selectedValues.longitude.toFixed(4)}
                                </p>
                            )}
                            {geoSuccess && (
                                <p className="location-success-message">✅ Location detected successfully.</p>
                            )}
                        </>
                    )}

                    {locationMode === 'list' && (
                        <>
                            <input
                                type="text"
                                className="location-search-input"
                                placeholder="Search location..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            <ul className="location-options-list">
                                {locationOptions
                                    .filter(loc => loc.name.toLowerCase().includes(searchTerm.toLowerCase()))
                                    .map(loc => (
                                        <li
                                            key={loc.id}
                                            className="location-option"
                                            onClick={() => onChange(loc)}
                                        >
                                            {loc.name}
                                        </li>
                                    ))}
                            </ul>
                            {selectedValues?.name && (
                                <p className="selected-location">Selected: {selectedValues.name}</p>
                            )}
                        </>
                    )}
                </div>
            )}

            {type === 'optionsSelector' && (
                <>
                    <div className="selector-search">
                        <input
                            type="text"
                            placeholder="Search..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>

                    {filteredOptions.length > 0 && (
                        <ul className="selector-options">
                            {filteredOptions.map((option, index) => (
                                <li key={index} onClick={() => handleSelect(option)}>
                                    {option.name || option}
                                </li>
                            ))}
                        </ul>
                    )}

                    <div className="selector-selected">
                        {(Array.isArray(selectedValues) ? selectedValues : [selectedValues])
                            .filter(Boolean)
                            .map((item, index) => (
                                <span key={index} className="selector-tag">
                                    {item.name || item}
                                    <button onClick={() => handleRemove(item)} className="selector-remove">
                                        &times;
                                    </button>
                                </span>
                            ))}
                    </div>
                </>
            )}
        </div>
    );
};

export default Selector;
