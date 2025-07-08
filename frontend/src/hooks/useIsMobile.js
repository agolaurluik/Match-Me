import { useState, useEffect } from 'react';

export default function useIsMobile(breakpoint = 1354) {
  const [isMobile, setIsMobile] = useState(window.innerWidth < breakpoint);

  useEffect(() => {
    const handleResize = () => {
      const newIsMobile = window.innerWidth < breakpoint;
    //   console.log(`[useIsMobile] width: ${window.innerWidth}, isMobile: ${newIsMobile}`);
      setIsMobile(newIsMobile);
    };

    window.addEventListener('resize', handleResize);

    handleResize();

    return () => window.removeEventListener('resize', handleResize);
  }, [breakpoint]);

  return isMobile;
}