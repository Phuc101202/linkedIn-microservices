package com.linkedln.searchservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkedln.searchservice.model.PostDocument;
import com.linkedln.searchservice.model.UserDocument;
import com.linkedln.searchservice.service.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/search")
@Slf4j
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Search people by name, headline or location
     */
    @GetMapping("/people")
    public ResponseEntity<List<UserDocument>> searchPeople(
            @RequestParam String q) {
        return ResponseEntity.ok(searchService.searchUsers(q));
    }

    /**
     * Search people by skill
     */
    @GetMapping("/skills")
    public ResponseEntity<List<UserDocument>> searchBySkill(
            @RequestParam String skill) {
        return ResponseEntity.ok(searchService.searchBySkill(skill));
    }

    /**
     * Search posts by content.
     * 
     * @param q
     * @return
     */
    @GetMapping("/posts")
    public ResponseEntity<List<PostDocument>> searchPosts(
            @RequestParam String q) {
        return ResponseEntity.ok(searchService.searchPosts(q));
    }
}
