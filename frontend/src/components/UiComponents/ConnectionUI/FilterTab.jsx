import React, { useState, useEffect } from "react";
import './FilterTab.css';
import Button from "../GeneralUI/Button";
import api from '../../../api/api';


export default function FilterTab({ onChange }) {
    const [filterData, setFilterData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [buttonText, setButtonText] = useState("UPDATE");

    useEffect(() => {
        async function loadFilter() {
            try {
                const response = await api.fetchMatchingFilter();
                const filter = response.matchingFilter;

                const filled = {
                    genderPreference: filter.genderPreference || 'lookingforall',
                    nationalityScore: filter.nationalityScore ?? 1,
                    interestScore: filter.interestScore ?? 1,
                    personalityScore: filter.personalityScore ?? 1,
                    purposeScore: filter.purposeScore ?? 1,

                    lowestAge: filter.lowestAge || 18,
                    highestAge: filter.highestAge || 99,

                    radius: filter.radius ?? 20000,
                };

                setFilterData(filled);
                if (onChange) onChange(filled);
            } catch (err) {
                console.error("Failed to load filter:", err);
                setError(err);
            } finally {
                setLoading(false);
            }
        }

        loadFilter();
    }, []);

    const handleChange = (field, value) => {
        let updated = {
            ...filterData,
            [field]: value
        };

        if (field === 'lowestAge') {
            updated.lowestAge = Math.min(value, filterData.highestAge);
        }

        if (field === 'highestAge') {
            updated.highestAge = Math.max(value, filterData.lowestAge);
        }

        setFilterData(updated);

        if (onChange) {
            onChange(updated);
        }
    };

    const scoreOptions = [
        { value: 0, label: "Not important" },
        { value: 1, label: "Normal" },
        { value: 2, label: "Very Important" }
    ];

    if (loading) return <div className="loading-container">Loading filter...</div>;
    if (error) return <div className="error-container">Failed to load filter.</div>;

    return (

        <div className="user-filter-grid">
            <div className="user-filter-grid-item">
                <label>
                    Gender preference:
                    <select
                        value={filterData.genderPreference}
                        onChange={(e) => handleChange('genderPreference', e.target.value)}
                    >
                        <option value="lookingformen">Male</option>
                        <option value="lookingforwomen">Female</option>
                        <option value="lookingforother">Other</option>
                        <option value="lookingforall">Not important</option>
                    </select>
                </label>

                <label>
                    Nationality score:
                    <select
                        value={filterData.nationalityScore}
                        onChange={(e) => handleChange('nationalityScore', parseInt(e.target.value))}
                    >
                        {scoreOptions.map(opt => (
                            <option key={opt.value} value={opt.value}>{opt.label}</option>
                        ))}
                    </select>
                </label>

                <label>
                    Interests Score:
                    <select
                        value={filterData.interestScore}
                        onChange={(e) => handleChange('interestScore', parseInt(e.target.value))}
                    >
                        {scoreOptions.map(opt => (
                            <option key={opt.value} value={opt.value}>{opt.label}</option>
                        ))}
                    </select>
                </label>

                <label>
                    Personality Score:
                    <select
                        value={filterData.personalityScore}
                        onChange={(e) => handleChange('personalityScore', parseInt(e.target.value))}
                    >
                        {scoreOptions.map(opt => (
                            <option key={opt.value} value={opt.value}>{opt.label}</option>
                        ))}
                    </select>
                </label>

                <label>
                    Purpose Score:
                    <select
                        value={filterData.purposeScore}
                        onChange={(e) => handleChange('purposeScore', parseInt(e.target.value))}
                    >
                        {scoreOptions.map(opt => (
                            <option key={opt.value} value={opt.value}>{opt.label}</option>
                        ))}
                    </select>
                </label>

                <label>
                    Age Range: {filterData.lowestAge} - {filterData.highestAge}
                    <div className="range-container">
                        <input type="range"
                            min="18"
                            max="100"
                            value={filterData.lowestAge}
                            onChange={(e) => handleChange('lowestAge', parseInt(e.target.value))}
                        />

                        <input type="range"
                            min="18"
                            max="100"
                            value={filterData.highestAge}
                            onChange={(e) => handleChange('highestAge', parseInt(e.target.value))}
                        />
                    </div>
                </label>

                <label>
                    Radius (km): {filterData.radius.toLocaleString()}
                    <input
                        type="range"
                        min="5"
                        max="20000"
                        step="5"
                        value={filterData.radius}
                        onChange={(e) => handleChange('radius', parseInt(e.target.value))}
                    />
                </label>
                <Button
                    onClick={async () => {
                        try {
                            await api.patchMatchingFilter(filterData);
                            // console.log("Filter updated", filterData);
                            setButtonText("Filter updated");

                            setTimeout(() => {
                                setButtonText("UPDATE");
                            }, 5000);
                        } catch (err) {
                            console.error("Error updating filter:", err);
                            setButtonText("Error! Try again");

                            setTimeout(() => {
                                setButtonText("UPDATE");
                            }, 5000);
                        }
                    }}
                    text={buttonText}
                    className="filter-submit"
                />
            </div>
        </div>
    )
}