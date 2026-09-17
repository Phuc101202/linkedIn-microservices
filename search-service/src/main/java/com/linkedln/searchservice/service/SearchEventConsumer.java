package com.linkedln.searchservice.service;

import java.util.List;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.linkedln.searchservice.model.PostDocument;
import com.linkedln.searchservice.model.UserDocument;
import com.linkedln.searchservice.repository.PostSearchRepository;
import com.linkedln.searchservice.repository.UserSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    @KafkaListener(topics = "user.created")
    public void consumeUserCreated(
            @Payload Map<String, Object> payload) {
        try {
            log.info("Indexing new user: {}", payload.get("userId"));

            UserDocument document = new UserDocument();
            document.setId((String) payload.get("userId"));
            document.setFirstName((String) payload.get("firstName"));
            document.setLastName((String) payload.get("lastName"));
            document.setEmail((String) payload.get("email"));
            document.setHeadline((String) payload.get("headline"));
            document.setLocation((String) payload.get("location"));

            userSearchRepository.save(document);
            log.info("User indexed: {}", payload.get("userId"));
        } catch (Exception e) {
            log.error("Error indexing user: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "user.updated")
    public void consumeUserUpdated(
            @Payload Map<String, Object> payload) {
        try {
            String userId = (String) payload.get("userId");
            log.info("Updating user index: {}", userId);

            userSearchRepository.findById(userId).ifPresent(doc -> {
                doc.setFirstName((String) payload.get("firstName"));
                doc.setLastName((String) payload.get("lastName"));
                doc.setHeadline((String) payload.get("headline"));
                doc.setLocation((String) payload.get("location"));

                if (payload.get("skills") != null) {
                    doc.setSkills((List<String>) payload.get("skills"));
                }

                userSearchRepository.save(doc);
                log.info("User index updated: {}", userId);
            });
        } catch (Exception e) {
            log.error("Error updating user Index: {}", e.getMessage());
        }
    }

    /**
     * consume post.created evenet
     */
    @KafkaListener(topics = "post.created")
    public void consumePostCreated(
            @Payload Map<String, Object> payload) {
        try {
            log.info("Indexing new user: {}", payload.get("userId"));

            PostDocument document = new PostDocument();
            document.setId((String) payload.get("postId"));
            document.setContent((String) payload.get("content"));
            document.setAuthorId((String) payload.get("authorId"));
            document.setImageUrl((String) payload.get("imageUrl"));
            document.setCreatedAt((String) payload.get("createdAt"));

            postSearchRepository.save(document);
        } catch (Exception e) {
            log.error("Error indexing post: {}", e.getMessage());
        }
    }
}
