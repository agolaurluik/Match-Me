import React from 'react';
import { Text } from '@react-three/drei';
import { a, useSpring } from '@react-spring/three';

const SceneText = ({ visible = true, isMobile= false }) => {
  const { position, opacity, scale } = useSpring({
    position: visible ? (isMobile ? [-1, -1.4, 0] : [1, 0, 0]) : [25, -10, 0],
    scale: visible ? (isMobile ? [0.7, 0.7, 0.7] : [1.2, 1.2, 1.2]) : [0, 0, 0],
    opacity: visible ? 1 : 0,
    config: { mass: 1, tension: 200, friction: 30 },
  });

    const textData = isMobile
    ? [
        { text: 'Energetic?', pos: [0, 2.2, 0], size: 0.28 },
        { text: 'Creative?', pos: [1.3, 2.0, 0], size: 0.26 },
        { text: 'Diplomatic?', pos: [2.6, 1.8, 0], size: 0.28 },
        { text: 'Honest?', pos: [0.8, 1.2, 0], size: 0.24 },
        { text: 'Charismatic?', pos: [2.4, 0.9, 0], size: 0.27 },
        { text: 'Original?', pos: [1.5, 0.2, 0], size: 0.26 },
        { text: 'Generous?', pos: [2.8, -0.5, 0], size: 0.25 },
        { text: 'Adventurous?', pos: [0.8, -1.0, 0], size: 0.25 },
        { text: 'Humoristic?', pos: [2.3, -1.6, 0], size: 0.25 },
        { text: 'Organized?', pos: [1.5, -2.2, 0], size: 0.24 },
      ]
    : [
        { text: 'Energetic?', pos: [0, 2.5, 0], size: 0.36 },
        { text: 'Creative?', pos: [1.5, 2.2, 0], size: 0.32 },
        { text: 'Diplomatic?', pos: [3.5, 2.0, 0], size: 0.34 },
        { text: 'Honest?', pos: [1.2, 1.0, 0], size: 0.30 },
        { text: 'Charismatic?', pos: [3.8, 0.5, 0], size: 0.35 },
        { text: 'Original?', pos: [2.5, 0.0, 0], size: 0.33 },
        { text: 'Generous?', pos: [4.5, -0.8, 0], size: 0.31 },
        { text: 'Adventurous?', pos: [1.0, -1.4, 0], size: 0.29 },
        { text: 'Humoristic?', pos: [3.0, -2.0, 0], size: 0.30 },
        { text: 'Organized?', pos: [2.5, -2.8, 0], size: 0.28 },
      ];

  return (
    <a.group position={position} style={{ opacity }} scale={scale}>
      {textData.map(({ text, pos, size }, i) => (
        <Text
          key={i}
          position={pos}
          fontSize={size}
          color={i % 2 === 0 ? '#b35900' : '#FFA64D'}
          anchorX="center"
          anchorY="middle"
          outlineWidth={0.02}
          outlineColor="#000"
        >
          {text}
        </Text>
      ))}
    </a.group>
  );
};

export default SceneText;
