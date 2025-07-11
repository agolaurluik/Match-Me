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
// import useIsMobile from '../../hooks/useIsMobile';


const Background = () => {
  const texture = useLoader(THREE.TextureLoader, '/textures/pencil-background.jpg');
  const { viewport, camera } = useThree();
  const meshRef = useRef();

  useEffect(() => {
    texture.minFilter = THREE.LinearFilter;
    texture.magFilter = THREE.LinearFilter;
    texture.wrapS = THREE.ClampToEdgeWrapping;
    texture.wrapT = THREE.ClampToEdgeWrapping;
  }, [texture]);

  // Scale mesh to cover screen (like background-size: cover)
  const aspect = texture.image ? texture.image.width / texture.image.height : 1;
  const screenAspect = viewport.width / viewport.height;

  const scale = aspect > screenAspect
    ? [viewport.height * aspect, viewport.height, 1] // wider image, height fits
    : [viewport.width, viewport.width / aspect, 1];  // taller image, width fits

  return (
    <mesh ref={meshRef} position={[0, 0, -5]} scale={scale}>
      <planeGeometry args={[2, 2]} />
      <meshBasicMaterial map={texture} />
    </mesh>
  );
};



const CameraController = () => {
  const { camera } = useThree();

  useEffect(() => {
    camera.position.set(0, 0, 5.5);
    camera.lookAt(0, 0, 0);
  }, [camera]);

  return null;
};

const Scene = ({ earthVisible, heartVisible, orbitGroupVisible, textVisible, connectTextVisible, isMobile, isTablet, isDesktop, meshPosition }) => {

  //  const isMobile = useIsMobile(); 
  

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
        <directionalLight position={[-200, 20, 200]} intensity={0.7}  />
        <Suspense fallback={null}>
          <Heart visible={heartVisible} isMobile={isMobile} isTablet={isTablet} isDesktop={isDesktop}  meshPosition={meshPosition}/>
          <OrbitingGroup visible={orbitGroupVisible} isMobile={isMobile} isTablet={isTablet} isDesktop={isDesktop} meshPosition={meshPosition} />
          <Text visible={textVisible} isMobile={isMobile} isTablet={isTablet} isDesktop={isDesktop} meshPosition={meshPosition}/>
          <ConnectTextMesh visible={connectTextVisible} isMobile={isMobile} isTablet={isTablet} isDesktop={isDesktop} meshPosition={meshPosition}/>

          <EarthMesh visible={earthVisible} isMobile={isMobile} isTablet={isTablet} isDesktop={isDesktop} meshPosition={meshPosition}/>

          <Background />
          <CameraController />
          <OrbitControls
            enableZoom={false}
            enableRotate={false}
            enablePan={false} />
        </Suspense>
      </Canvas>
    </div>
  );
};

export default Scene;