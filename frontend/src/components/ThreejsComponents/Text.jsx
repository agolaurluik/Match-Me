import React from 'react';
import { Text } from '@react-three/drei';
import { a, useSpring } from '@react-spring/three';

const SceneText = ({ visible = true, isMobile= false }) => {
  const { position, opacity, scale } = useSpring({
    position: visible ? (isMobile ? [-1.7, -1, 0] : [0, 0, 0]) : [25, -10, 0],
    scale: visible ? (isMobile ? [0.7, 0.7, 0.7] : [1.2, 1.2, 1.2]) : [0, 0, 0],
    opacity: visible ? 1 : 0,
    config: { mass: 1, tension: 200, friction: 30 },
  });

  return (
    <a.group position={position} style={{ opacity }} scale={scale}>
      <Text position={[0, 2.5, 0]} fontSize={0.36} color="#cc7a00" anchorX="center" anchorY="middle">
        Energetic?
      </Text>

      <Text position={[1.5, 2.2, 0]} fontSize={0.32} color="#e68a00" anchorX="center" anchorY="middle">
        Creative?
      </Text>

      <Text position={[3.5, 2.0, 0]} fontSize={0.34} color="#ff9933" anchorX="center" anchorY="middle">
        Diplomatic?
      </Text>

      <Text position={[1.2, 1.0, 0]} fontSize={0.30} color="#cc6600" anchorX="center" anchorY="middle">
        Honest?
      </Text>

      <Text position={[3.8, 0.5, 0]} fontSize={0.35} color="#b35900" anchorX="center" anchorY="middle">
        Charismatic?
      </Text>

      <Text position={[2.5, 0.0, 0]} fontSize={0.33} color="#d97700" anchorX="center" anchorY="middle">
        Original?
      </Text>

      <Text position={[4.5, -0.8, 0]} fontSize={0.31} color="#994d00" anchorX="center" anchorY="middle">
        Generous?
      </Text>

      <Text position={[1.0, -1.4, 0]} fontSize={0.29} color="#cc6600" anchorX="center" anchorY="middle">
        Adventurous?
      </Text>

      <Text position={[3.0, -2.0, 0]} fontSize={0.30} color="#e67300" anchorX="center" anchorY="middle">
        Humoristic?
      </Text>

      <Text position={[2.5, -2.8, 0]} fontSize={0.28} color="#b36b00" anchorX="center" anchorY="middle">
        Organized?
      </Text>
    </a.group>
  );
};

export default SceneText;
