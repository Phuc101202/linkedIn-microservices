package com.linkedln.searchservice.repository;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.linkedln.searchservice.model.PostDocument;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String> {
    @Query("{\"match\" : {\"content\" :  {\"query\" : \"?0\" , " + "\"fizziness\" : \"AUTO\"}}}")
    List<PostDocument> searchPosts(String query);
}
