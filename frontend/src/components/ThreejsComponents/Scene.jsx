import React, { useEffect, useRef, useState } from 'react';
import { Canvas, useLoader, useThree, useFrame } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import { Suspense } from 'react';
import * as THREE from 'three';

import Heart from './Heart';
import Ball from './Ball';
import Pencils from './Pencils';
import Text from './Text';
import EarthMesh from './EarthMesh'; 
import ConnectTextMesh from './ConnectText';
import OrbitingGroup from './OrbitingGroup';
import useIsMobile from '../../hooks/useIsMobile';


const Background = () => {
  const { scene } = useThree();
  const texture = useLoader(THREE.TextureLoader, '/textures/image.jpg');
  const isMobile = useIsMobile();

  useEffect(() => {
    scene.background = texture;
  }, [texture, scene]);

  return null;
};


const CameraController = () => {
  const { camera } = useThree();

  useEffect(() => {
    camera.position.set(0, 0, 5.5);
    camera.lookAt(0, 0, 0);
  }, [camera]);

  return null;
};

const Scene = ({ earthVisible, heartVisible, orbitGroupVisible, textVisible, connectTextVisible }) => {

   const isMobile = useIsMobile(); 
  

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100vh',
      zIndex: -1,
    }}>
      <Canvas>
        <ambientLight intensity={2} />
        <directionalLight position={[-200, 20, 200]} intensity={1.0}  />
        <Suspense fallback={null}>
          <Heart visible={heartVisible} isMobile={isMobile}/>
          <OrbitingGroup visible={orbitGroupVisible} isMobile={isMobile} />
          <Text visible={textVisible} isMobile={isMobile} />
          <ConnectTextMesh visible={connectTextVisible} isMobile={isMobile}/>

          <EarthMesh visible={earthVisible} isMobile={isMobile} />

          <Background />
          <CameraController />
          <OrbitControls />
        </Suspense>
      </Canvas>
    </div>
  );
};

export default Scene;