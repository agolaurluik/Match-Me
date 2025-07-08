import { useEffect, useState } from 'react';
import api from '../api/api';

export default function useOptions() {
  const [options, setOptions] = useState({
    personalities: [],
    interests: [],
    nationalities: [],
    purposes: [],
    genders: [],
    locations: [],
  });

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchOptions() {
      try {
        const [personalitiesRes, interestsRes, nationalitiesRes, purposesRes, gendersRes, locationsRes] = await Promise.all([
          api.fetchPersonalities(),
          api.fetchInterests(),
          api.fetchNationalities(),
          api.fetchPurposes(),
          api.fetchGenders(),
          api.fetchLocations(),
        ]);
        setOptions({ 
          personalities: personalitiesRes.personalities || [],
          interests: interestsRes.interests || [],
          nationalities: nationalitiesRes.nationalities || [],
          purposes: purposesRes.purposes || [],
          genders: gendersRes.genders || [],
          locations: locationsRes.namedLocations || [],
        });
      } catch (err) {
        console.error('Failed to fetch options:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchOptions();
  }, []);

  return { ...options, loading };
}