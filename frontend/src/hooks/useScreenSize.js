import { useState, useEffect } from 'react';

export default function useScreenSize() {
  const getSize = () => {
    const width = window.innerWidth;
    if (width < 740) return 'mobile';       // mobile < 768
    if (width < 1024) return 'tablet';      // tablet 768 <= width < 1024
    return 'desktop';                        // desktop >= 1024
  };

  const [screenSize, setScreenSize] = useState(getSize());

  useEffect(() => {
    const handleResize = () => setScreenSize(getSize());
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return screenSize; // 'mobile', 'tablet', 'desktop'
}