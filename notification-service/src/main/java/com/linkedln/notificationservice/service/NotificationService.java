package com.linkedln.notificationservice.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    /**
     * Consume user.created event
     * Send welcome notification
     */
    @KafkaListener(topics = "user.created")
    public void consumeUserCreated(
            @Payload Map<String, Object> payload) {
        try {
            String userId = (String) payload.get("userId");
            String firstName = (String) payload.get("firstName");

            sendNotification(userId,
                    "Welcome to Linkedln!",
                    String.format(
                            "Welcome %s Your account has been created" +
                                    "Start connecting with professionals.",
                            firstName));
        } catch (Exception e) {
            log.error("Error sending welcome notification: {}", e.getMessage());
        }
    }

    /**
     * Consume connection.requested event
     * Notify receiver about connection request.
     */
    @KafkaListener(topics = "connection.requested")
    public void consumeConnectionRequest(
            @Payload Map<String, Object> payload) {
        try {
            String receiverId = (String) payload.get("receiverId");
            String requesterId = (String) payload.get("requesterId");

            sendNotification(receiverId, "New Connection Request",
                    String.format("User %s want to connect with you.", requesterId));

        } catch (Exception e) {
            log.error("Error sending connection request notification: {}", e.getMessage());
        }
    }

    /**
     * Consume connection.accepted event.
     * Notify requester that connection was accepted
     */
    @KafkaListener(topics = "connection.accepted")
    public void consumeConnectionAccept(
            @Payload Map<String, Object> payload) {
        try {
            String receiverId = (String) payload.get("receiverId");
            String requesterId = (String) payload.get("requesterId");

            sendNotification(requesterId, "Connection Accepted",
                    String.format("User %s accepted your connection request " + "You are now connected", requesterId));

        } catch (Exception e) {
            log.error("Error sending connection accepted notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "post.liked")
    public void consumePostLiked(
            @Payload Map<String, Object> payload) {
        try {
            String authorId = (String) payload.get("authorId");
            String userId = (String) payload.get("userId");
            String postId = (String) payload.get("postId");

            sendNotification(authorId, "Someone liked your post",
                    String.format("User %s liked your post %s", userId, postId));
        } catch (Exception e) {
            log.error("Error sending like notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "post.commented")
    public void consumePostCommented(
            @Payload Map<String, Object> payload) {
        try {
            String postAuthorId = (String) payload.get("postAuthorId");
            String commenterId = (String) payload.get("authorId");
            String postId = (String) payload.get("postId");

            sendNotification(postAuthorId, "Someone comment on your post",
                    String.format("User %s comment on your post %s", commenterId, postId));
        } catch (Exception e) {
            log.error("Error sending c notification: {}", e.getMessage());
        }
    }

    private void sendNotification(String userId, String title, String message) {
        log.info("------------------------------------");
        log.info("NOTIFICATION SENT");
        log.info("To User: {}", userId);
        log.info("Title: {}", title);
        log.info("Message: {}", message);
        log.info("------------------------------------");
    }
}
