package com.linkedln.postservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkedln.postservice.entity.Post;

public interface PostRepository extends JpaRepository<Post, String> {
    List<Post> findByAuthorIdOrderByCreatedAtDesc(String userId);
}
