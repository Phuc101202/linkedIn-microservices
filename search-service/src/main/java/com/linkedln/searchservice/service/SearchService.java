package com.linkedln.searchservice.service;

import java.util.List;

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
public class SearchService {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    public List<UserDocument> searchUsers(String query) {
        log.info("Searching users: {}", query);
        return userSearchRepository.searchUsers(query);
    }

    /**
     * Search users by skill
     */

    public List<UserDocument> searchBySkill(String skill) {
        log.info("Searching users by skill: {}", skill);
        return userSearchRepository.findBySkillsContaining(skill);
    }

    /**
     * Search posts by content
     * 
     * @param query
     * @return
     */
    public List<PostDocument> searchPosts(String query) {
        log.info("Searching posts: {}", query);
        return postSearchRepository.searchPosts(query);
    }
}
