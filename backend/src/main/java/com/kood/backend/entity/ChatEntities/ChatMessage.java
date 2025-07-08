package com.kood.backend.entity.ChatEntities;

import java.time.Instant;

import com.kood.backend.entity.ConnectionEntities.Connection;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "connection_id")
    private Connection connection;

    private Long sender;

    private Long receiver;

    private String content;

    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;
}
