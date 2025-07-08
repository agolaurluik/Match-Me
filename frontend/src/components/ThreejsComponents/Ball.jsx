import React, { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import { useGLTF } from '@react-three/drei';
import { a, useSpring } from '@react-spring/three';

const Ball = ({ visible = true, ...props }) => {
  const { scene } = useGLTF('/meshes/ball.glb');
  const ballRef = useRef();

  const { position, opacity } = useSpring({
    position: visible ? [5, -1, -2] : [20, -10.0, -2],
    opacity: visible ? 1 : 0,
    config: { mass: 1, tension: 200, friction: 30 },
  });

  // useFrame(() => {
  //   if (ballRef.current) {
  //     ballRef.current.rotation.y += 0.001;
  //   }
  // });

  return (
    <a.primitive
      object={scene}
      ref={ballRef}
      position={position}
      scale={[1.1, 1.1, 1.1]}
      {...props}
    />
  );
};

export default Ball;
