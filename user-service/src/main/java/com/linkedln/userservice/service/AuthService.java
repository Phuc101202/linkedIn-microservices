package com.linkedln.userservice.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.linkedln.userservice.dto.AuthResponse;
import com.linkedln.userservice.dto.LoginRequest;
import com.linkedln.userservice.dto.RegisterRequest;
import com.linkedln.userservice.entity.User;
import com.linkedln.userservice.entity.UserRole;
import com.linkedln.userservice.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("{jwt.secret}")
    private String secretKey;

    @Value("{jwt.expiration}")
    private long jwtExpiration;

    @Value("{jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String USER_CREATED_TOPIC = "user.created";

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setHeadline(request.getHeadline());
        user.setLocation(request.getLocation());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.NORMAL_USER);

        User savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getId());

        Map<String, Object> userCreatedEvent = new HashMap<>();
        userCreatedEvent.put("userId", savedUser.getId());
        userCreatedEvent.put("firstName", savedUser.getFirstName());
        userCreatedEvent.put("lastName", savedUser.getLastName());
        userCreatedEvent.put("email", savedUser.getEmail());
        userCreatedEvent.put("headline", savedUser.getHeadline());
        userCreatedEvent.put("location", savedUser.getLocation());

        kafkaTemplate.send(USER_CREATED_TOPIC, savedUser.getId(), userCreatedEvent);
        log.info("user.created event published: {}", savedUser.getId());

        String token = generateToken(savedUser.getId(), savedUser.getEmail());

        return buildAuthResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "Email already registered: " + request.getEmail()));

        // Bcrypt verify - compare raw password with stored hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        log.info("Login successful: {}", user.getId());

        // Generate JWT Token
        String token = generateToken(user.getId(), user.getEmail());

        return buildAuthResponse(user, token);
    }

    /**
     * Generated access token
     * 
     * @param userId
     * @param email
     * @return
     */
    private String generateToken(String userId, String email) {
        return Jwts.builder()
                .claim("userId", userId)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate refresh token
     * Used to get a new access token when it expires
     * Server validates and return the new access token.
     * Client sends refresh token to /auth/refresh-token
     * 
     * @param userId
     * @return
     */
    private String generateRefreshToken(String userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setRefreshToken(generateRefreshToken(user.getId()));
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());

        return response;
    }
}
