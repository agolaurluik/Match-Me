import React, { useRef, useEffect, useState } from 'react';
import { useGLTF } from '@react-three/drei';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { a, useSpring } from '@react-spring/three';

const Heart = ({ visible = true, isMobile = false, ...props }) => {
  const { scene } = useGLTF('/meshes/heart3.glb');
  const heartRef = useRef();
  const clockRef = useRef(new THREE.Clock());
  const [heartbeatScale, setHeartbeatScale] = useState(1);

  const baseScale = visible
    ? isMobile
      ? [0.7, 0.7, 0.7]
      : [1.0, 1.0, 1.0]
      : [0, 0, 0];

  const { position, scale: springScale } = useSpring({
    position: visible ? (isMobile ? [0, -2, 0] : [4, 0, 0]) : [25, -10, 0],
    scale: baseScale,
    config: { mass: 1, tension: 200, friction: 30 },
  });

useEffect(() => {
  if (heartRef.current) {
    heartRef.current.rotation.y = isMobile ? 3 : 2.3;
  }

    scene.traverse((child) => {
      if (child.isMesh) {
        child.material = new THREE.MeshStandardMaterial({
          color: 0xff0000,
          metalness: 0.7,
          roughness: 0.5,
          envMapIntensity: 1.5,
          transparent: true,
          opacity: 1,
        });
        child.castShadow = true;
        child.receiveShadow = true;
      }
    });
  }, [scene]);

  useFrame(() => {
    const time = clockRef.current.getElapsedTime();
    const bpm = 55;
    const beatDuration = 60 / bpm;
    const t = time % beatDuration;

    let beatScale = 1;
    if (t < beatDuration * 0.2) {
      beatScale += Math.exp(-30 * t) * 0.15;
    } else if (t < beatDuration * 0.4) {
      const t2 = t - beatDuration * 0.2;
      beatScale += Math.exp(-30 * t2) * 0.02;
    }

    setHeartbeatScale(beatScale);
  });

  return (
    <a.primitive
      object={scene}
      ref={heartRef}
      position={position}
      scale={springScale.to((x, y, z) => [
        x * heartbeatScale,
        y * heartbeatScale,
        z * heartbeatScale,
      ])}
      {...props}
    />
  );
};

export default Heart;