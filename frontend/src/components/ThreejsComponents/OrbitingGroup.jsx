import React, { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import { a, useSpring } from '@react-spring/three';
import Pencils from './Pencils';
import Ball from './Ball';

const OrbitingGroup = ({ visible = true, isMobile = false, isTablet= false, isDesktop=true }) => {
  const innerGroupRef = useRef();


  const { position, scale } = useSpring({
    position: visible
      ? (isMobile ? [0, -2, 0] : (isTablet || isDesktop ? [4, 0, 0] : [4, 0, 0]))
      : [25, -10, 0],
    scale: visible
      ? (isMobile ? [0.7, 0.7, 0.7] : (isTablet || isDesktop ? [1.0, 1.0, 1.0] : [1, 1, 1]))
      : [0, 0, 0],
    config: { mass: 1, tension: 200, friction: 30 },
  });


  useFrame(() => {
    if (innerGroupRef.current) {
      innerGroupRef.current.rotation.y += 0.00;
    }
  });

  return (
    <a.group position={position} scale={scale} >
      <group ref={innerGroupRef}>
        <Pencils visible={visible} position={[1.35, 0, 0]} />
        <Ball visible={visible} position={[-0.65, 0, 0]} />
      </group>
    </a.group>
  );
};

export default OrbitingGroup;
