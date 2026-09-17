package com.linkedln.feedservice.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.linkedln.client.UserServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedEventConsumer {

    private final UserServiceClient userServiceClient;
    private static final String FEED_KEY_PREFIX = "feed:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${feed.max-size:100}")
    private int maxFeedSize;

    /**
     * Consume post.created event
     * When a user creates a post - immediately push that post
     * to All their connections feed.
     * 
     * This is how Linkedln / Twitter generated feed.
     * 
     * @param payload
     */
    @KafkaListener(topics = "post.created")
    public void consumePostCreated(
            @Payload Map<String, Object> payload) {
        try {
            String postId = (String) payload.get("postId");
            String authorId = (String) payload.get("authorId");

            // Get all connections of the post author
            List<Map<String, Object>> connections = userServiceClient.getConnections(authorId);

            // Push post to each connection's feed/
            for (Map<String, Object> connection : connections) {
                String connectionId = (String) connection.get("id");
                String feedKey = FEED_KEY_PREFIX + connectionId;

                // Add post to feed (as sorted set - score = timestamp)
                redisTemplate.opsForList().leftPush(feedKey, postId);

                redisTemplate.opsForList().trim(feedKey, 0, maxFeedSize - 1);

                log.info("Post {} pushed to feed of user: {}", postId, connectionId);
            }

            String authorFeedKey = FEED_KEY_PREFIX + authorId;
            redisTemplate.opsForList().leftPush(authorFeedKey, postId);
            redisTemplate.opsForList().trim(authorFeedKey, 0, maxFeedSize - 1);
        } catch (Exception e) {
            log.error("Error is pushing: {}", e.getMessage());
        }
    }
}
