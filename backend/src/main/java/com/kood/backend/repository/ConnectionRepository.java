package com.kood.backend.repository;

import com.kood.backend.entity.ConnectionEntities.Connection;
import com.kood.backend.entity.ConnectionEntities.ConnectionStatus;
import com.kood.backend.entity.UserEntities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

  Optional<Connection> findBySenderAndReceiver(Long senderId, Long receiverId);

  List<Connection> findAllBySender(Long senderId);

  List<Connection> findAllByReceiver(Long receiverId);

  List<Connection> findByReceiverAndReceiverStatus(Long receiver, ConnectionStatus status);

  List<Connection> findBySenderAndSenderStatus(Long sender, ConnectionStatus status);

  @Query("SELECT c FROM Connection c WHERE (c.sender = :user OR c.receiver = :user)")
  List<Connection> findAllBySenderOrReceiver(@Param("user") User user);

  @Query(value = """
          SELECT * FROM connection
          WHERE sender_status = 'ACCEPTED'
            AND receiver_status = 'ACCEPTED'
            AND ((sender_id = :id1 AND receiver_id = :id2)
              OR (sender_id = :id2 AND receiver_id = :id1))
      """, nativeQuery = true)
  Optional<Connection> findByAcceptedParticipants(@Param("id1") Long id1, @Param("id2") Long id2);

}