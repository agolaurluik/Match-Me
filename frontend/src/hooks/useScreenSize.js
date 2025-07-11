import { useState, useEffect } from 'react';

export default function useScreenSize() {
  const getSize = () => {
    const width = window.innerWidth;
    if (width < 1424) return 'mobile';
    // if (width < 1024) return 'tablet';
    return 'desktop';
  };

  const [screenSize, setScreenSize] = useState(getSize());

  useEffect(() => {
    const handleResize = () => setScreenSize(getSize());
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return screenSize; // 'mobile', 'tablet', 'desktop'
}