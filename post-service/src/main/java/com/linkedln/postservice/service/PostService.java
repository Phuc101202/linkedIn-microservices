package com.linkedln.postservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.linkedln.postservice.entity.Comment;
import com.linkedln.postservice.entity.Like;
import com.linkedln.postservice.entity.Post;
import com.linkedln.postservice.repository.CommentRepository;
import com.linkedln.postservice.repository.LikeRepository;
import com.linkedln.postservice.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String POST_CREATED_TOPIC = "post.created";
    private static final String POST_LIKED_TOPIC = "post.liked";
    private static final String POST_COMMENTED_TOPIC = "post.commented";

    /**
     * Create a post
     * Optionally upload to S3.
     * Publish post.created event to Kafka
     * feed Service and Search Service will consume this
     */

    public Post createPost(String authorId, String content, MultipartFile image) {
        log.info("Creating post for user: {}", authorId);

        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(content);

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image, "posts/" + authorId);

            post.setImageUrl(imageUrl);
        }

        Post savedPost = postRepository.save(post);
        log.info("Post created: {}", savedPost.getId());

        // Publish to kafka - feed Service and Search Service
        Map<String, Object> postCreatedEvent = new HashMap<>();
        postCreatedEvent.put("postId", savedPost.getId());
        postCreatedEvent.put("authorId", savedPost.getAuthorId());
        postCreatedEvent.put("content", savedPost.getContent());
        postCreatedEvent.put("imageUrl", savedPost.getImageUrl());
        postCreatedEvent.put("createdAt", savedPost.getCreatedAt().toString());

        kafkaTemplate.send(POST_CREATED_TOPIC, savedPost.getId(), postCreatedEvent);
        log.info("post.created event published: {}", savedPost.getId());
        return savedPost;
    }

    public Post getPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(
                        "Post not found: " + postId));
    }

    public List<Post> getUserPosts(String userId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    /**
     * like or unilike a post
     * 
     * @param postId
     * @param userId
     * @return
     */
    public String likePost(String postId, String userId) {
        Post post = getPost(postId);

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {

            // Unlike
            likeRepository.findByPostIdAndUserId(postId, userId)
                    .ifPresent(likeRepository::delete);
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
            return "Post unlike";
        }

        // like
        Like like = new Like();
        like.setPostId(postId);
        like.setUserId(userId);
        likeRepository.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        // Publish post.liked event
        Map<String, Object> postLikedEvent = new HashMap<>();
        postLikedEvent.put("postId", postId);
        postLikedEvent.put("userId", userId);
        postLikedEvent.put("authorId", post.getAuthorId());

        kafkaTemplate.send(POST_LIKED_TOPIC, postId, postLikedEvent);

        return "Post liked";
    }

    /**
     * Add comment to post
     */

    public Comment addComment(String postId, String authorId, String content) {
        Post post = getPost(postId);

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setContent(content);
        comment.setAuthorId(authorId);

        Comment savedComment = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        // Publish comment.created event
        Map<String, Object> postCommentedEvent = new HashMap<>();
        postCommentedEvent.put("postId", postId);
        postCommentedEvent.put("commentId", savedComment.getId());
        postCommentedEvent.put("authorId", authorId);
        postCommentedEvent.put("postAuthorId", post.getAuthorId());

        kafkaTemplate.send(POST_COMMENTED_TOPIC, postId, postCommentedEvent);
        return savedComment;
    }

    /**
     * Get comment for a post
     * 
     * @param postId
     * @return
     */
    public List<Comment> getComments(String postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    /**
     * Delete post
     * 
     * @param postId
     * @param userId
     * @return
     */

    public void deletePost(String postId, String userId) {
        Post post = getPost(postId);

        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this post");
        }
        postRepository.delete(post);
        log.info("Post deleted: {}", postId);
    }
}
