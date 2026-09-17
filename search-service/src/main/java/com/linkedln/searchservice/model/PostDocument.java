package com.linkedln.searchservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * document for post search
 * Indexed when a post is created
 * Enables full text search across post content.
 */

@Document(indexName = "posts", createIndex = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Keyword)
    private String authorId;

    private String imageUrl;
    private String createdAt;
}
