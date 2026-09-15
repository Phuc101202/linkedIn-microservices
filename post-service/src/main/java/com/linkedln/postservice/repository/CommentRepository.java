package com.linkedln.postservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkedln.postservice.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(String postId);
}
