package com.kood.backend.entity.ConnectionEntities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long sender;

    @Column(name = "receiver_id")
    private Long receiver;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp // What is this? Does this do the onCreate by itself?
    private Instant createdAt;

    private Instant lastMessageTimestamp;

    private int numberOfMessages;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus senderStatus;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus receiverStatus;

    @PrePersist // Runs before persisting(saving) to the database
    protected void onCreate() {
        createdAt = Instant.now();
    }

}