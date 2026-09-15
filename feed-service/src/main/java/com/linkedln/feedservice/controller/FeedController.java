package com.linkedln.feedservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkedln.feedservice.service.FeedService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/feed")
@Slf4j
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * Get paginated feed for a user
     * Return list of Post IDs.
     * Client fetched full post details from Post Service
     * 
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<String>> getFeed(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(feedService.getFeed(userId, page, size));
    }

    /**
     * Clear feed cache - useful for testing
     * 
     * @param userId
     * @return
     */
    @DeleteMapping("/{userId}/cache")
    public ResponseEntity<String> clearFeed(
            @PathVariable String userId) {
        feedService.clearFeed(userId);

        return ResponseEntity.ok("Feed cache cleared");
    }
}
