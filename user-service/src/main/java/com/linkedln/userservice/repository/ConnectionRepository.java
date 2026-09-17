package com.linkedln.userservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkedln.userservice.entity.Connection;
import com.linkedln.userservice.entity.ConnectionStatus;

public interface ConnectionRepository extends JpaRepository<Connection, String> {
    boolean existsByRequesterIdAndReceiverId(String requesterId, String receiverId);

    List<Connection> findByRequesterIdAndStatus(String requesterId, ConnectionStatus status);

    List<Connection> findByReceiverIdAndStatus(String requesterId, ConnectionStatus status);

}
