package com.linkedln.searchservice.repository;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.linkedln.searchservice.model.UserDocument;

public interface UserSearchRepository extends
                ElasticsearchRepository<UserDocument, String> {
        @Query("{\"multi_match\" : {\"query\" : \"?0\", " + "\"fields\" : [\"firstName\", \"lastName\","
                        + "\"headline\", \"location\"]}}")
        List<UserDocument> searchUsers(String query);

        /**
         * Search by specific skill
         * 
         * @param skill
         * @return
         */
        List<UserDocument> findBySkillsContaining(String skill);
}
