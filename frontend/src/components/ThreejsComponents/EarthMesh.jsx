import React, { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import { useGLTF } from '@react-three/drei';
import { a, useSpring } from '@react-spring/three';

const EarthMesh = ({visible, isMobile = false ,...props}) => {
    const earthmesh = useGLTF('/meshes/earthmesh.glb')
    const earthRef = useRef();

    const { position, opacity, scale } = useSpring({
    position: visible ? (isMobile ? [0, -1.7, 0] : [4, 0, 0]) : [25, -10, 0],
    opacity: visible ? 1 : 0,
    scale: visible ? (isMobile ? [1, 1, 1] : [1.2, 1.2, 1.2]) : [0, 0, 0],
    config: { mass: 1, tension: 200, friction: 30 },
  });

    useFrame(() => {
        if(earthRef.current) {
            earthRef.current.rotation.y += 0.003;
        }
    });

    return(
    <a.primitive
        object={earthmesh.scene}
        ref={earthRef}
        position={position}
        scale={scale}
        {...props}
    />
    );
}

export default EarthMesh;