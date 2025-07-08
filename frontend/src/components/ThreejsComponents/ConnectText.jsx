import React, { useRef, useEffect } from 'react';
import { useGLTF } from '@react-three/drei';
import { a, useSpring } from '@react-spring/three';

const ConnectTextMesh = ({ visible = true,isMobile = false , ...props }) => {
  const connectText = useGLTF('/meshes/ConnectText.glb');
  const connectRef = useRef();

  const { position, opacity, scale } = useSpring({
     position: visible ? (isMobile ? [-1, -2, 0] : [2, 0, 0]) : [25, -10, -0],
     scale: visible ? (isMobile ? [0.8, 0.8, 0.8] : [1.2, 1.2, 1.2]) : [0, 0, 0],
    opacity: visible ? 1 : 0,
    config: { mass: 1, tension: 200, friction: 30 },
  });

  useEffect(() => {
    if (connectRef.current) {
      connectRef.current.rotation.x = Math.PI / 2; // 90 degrees
      connectRef.current.rotation.z = 0.15;
    }
  }, []);

  return (
    <a.primitive
      object={connectText.scene}
      ref={connectRef}
      position={position}
      scale={scale}
      style={{ opacity }}
      {...props}
    />
  );
};

export default ConnectTextMesh;
