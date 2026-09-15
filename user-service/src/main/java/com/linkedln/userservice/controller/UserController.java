package com.linkedln.userservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.linkedln.userservice.dto.UserResponse;
import com.linkedln.userservice.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Get user Profile
     * X-user-Id = requesting user (from Gateway)
     * userId in path = target user to fetch
     * 
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(
            @PathVariable String userId,
            @RequestHeader("X-User_Id") String requestingUserId) {
        log.info("Get profile: {} requested by: {}",
                userId, requestingUserId);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    /**
     * Update own profile
     * user can only update their own profile
     * 
     * @param userId
     * @param requestingUserId
     * @return
     */
    @PostMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String userId,
            @RequestHeader("X-User_Id") String requestingUserId,
            @RequestBody UserResponse request) {
        if (!userId.equals(requestingUserId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<UserResponse> uploadProfilePhoto(
            @PathVariable String userId,
            @RequestHeader("X-user-Id") String requestUserId,
            @RequestParam("file") MultipartFile file) {
        if (!userId.equals(requestUserId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                userService.uploadProfilePhoto(userId, file));
    }

    /**
     * Send connection request
     * Requester Id comes from X-User-Id header - already validated
     * 
     * @param targetUserId
     * @param requestingUserId
     * @return
     */
    @PostMapping("/{targetUserId}/connect")
    public ResponseEntity<String> sendConnectionRequest(
            @PathVariable String targetUserId,
            @RequestHeader("X-User-Id") String requestingUserId) {
        return ResponseEntity.ok(userService.sendConnectionRequest(
                targetUserId, requestingUserId));
    }

    @PutMapping("path/{id}")
    public ResponseEntity<String> acceptConnection(
            @PathVariable String connectionId,
            @RequestHeader("X-User-Id") String requestingUserId) {
        return ResponseEntity.ok(
                userService.acceptConnectionRequest(connectionId));
    }

    @GetMapping("/{userId}/connections")
    public ResponseEntity<List<UserResponse>> getConnection(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                userService.getConnections(userId));
    }
}
