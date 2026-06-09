package com.vgu.dwc.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.vgu.dwc.api.model.Article;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {
    // Chỉ cần để trống. Spring Boot sẽ tự động "hô biến" ra các hàm save(), findAll()!
}