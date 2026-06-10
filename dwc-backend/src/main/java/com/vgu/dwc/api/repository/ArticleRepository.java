package com.vgu.dwc.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.vgu.dwc.api.model.Article;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {
    // Chỉ cần để trống. Spring Boot sẽ tự động "hô biến" ra các hàm save(), findAll()!
	long countBySourceDomain(String sourceDomain); // Đếm số bài cào thành công theo tên miền
	
	// THÊM HÀM MỚI: Lấy danh sách bài viết, sắp xếp mới nhất
	Page<Article> findAllByOrderByCrawledAtDesc(Pageable pageable);
	
	// THÊM HÀM MỚI: Tìm kiếm theo Tiêu đề (chứa từ khóa) VÀ Domain (Bỏ qua chữ hoa/thường)
    Page<Article> findByTitleContainingIgnoreCaseAndSourceDomainContainingIgnoreCaseOrderByCrawledAtDesc(
            String keyword, String domain, Pageable pageable);
}