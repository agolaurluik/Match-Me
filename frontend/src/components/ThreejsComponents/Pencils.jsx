import React, { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import { useGLTF } from '@react-three/drei';
import { a, useSpring } from '@react-spring/three';

const Pencils = ({ visible = true, ...props }) => {
  const { scene } = useGLTF('/meshes/pencils.glb');
  const pencilsRef = useRef();

  // Spring animation
  const { position, opacity } = useSpring({
    position: visible ? [4.5, 0, 0] : [20, -10.0, 0], // animate in/out from right
    opacity: visible ? 1 : 0,
    config: { mass: 1, tension: 200, friction: 30 },
  });

  
  // useFrame(() => {
  //   if (pencilsRef.current) {
  //     pencilsRef.current.rotation.y += 0.002; 
  //   }
  // });

  return (
    <a.primitive
      object={scene}
      ref={pencilsRef}
      position={position}
      scale={[1.2, 1.2, 1.2]}
      {...props}
    />
  );
};

export default Pencils;
