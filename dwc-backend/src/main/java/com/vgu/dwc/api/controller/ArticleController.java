package com.vgu.dwc.api.controller;

import com.vgu.dwc.api.model.Article;
import com.vgu.dwc.api.repository.ArticleRepository;
import com.vgu.dwc.api.dto.StatsResponseDTO;
import com.vgu.dwc.api.service.RedisQueueService;
import com.vgu.dwc.crawler.queue.RedisQueueManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController // Khai báo đây là một API Server trả về JSON
@RequestMapping("/api/articles") // Đường dẫn gốc
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository; // Gọi cái "Cần cẩu" DB ra để dùng
    
    @Autowired
    private RedisQueueService redisQueueService; // Gọi anh Service vào làm việc
    
    @Autowired
    private RedisQueueManager queueManager;

    // 1. API cho Phú (Frontend) lấy toàn bộ danh sách bài báo
    // Phú sẽ gọi đường dẫn: GET http://localhost:8080/api/articles
    @GetMapping
    public List<Article> getAllArticles() {
        return articleRepository.findAll(); // Lấy tất cả từ MongoDB trả về
    }

    // 2. API xem thống kê tổng số bài báo đã cào được
    // Phú sẽ gọi: GET http://localhost:8080/api/articles/count
    @GetMapping("/count")
    public long getArticleCount() {
        return articleRepository.count(); // Đếm tổng số record trong DB
    }
    
    // 3. API cho Crawler (Huy) gửi bài báo mới cào được vào Database
    // Huy sẽ gọi: POST http://localhost:8080/api/articles
    @PostMapping
    public Article addArticle(@RequestBody Article newArticle) {
        // Hàm save() này cũng là do MongoRepository cung cấp sẵn!
        return articleRepository.save(newArticle);
    }
    
 // CẬP NHẬT HÀM THỐNG KÊ CHO DASHBOARD
    @GetMapping("/stats")
    public StatsResponseDTO getSystemStats() {
        long totalInDb = articleRepository.count(); // Đếm DB
        long pendingInRedis = redisQueueService.getQueueSize(); // Đếm Redis
        
        // Đóng gói thành DTO và gửi cho Phú
        return new StatsResponseDTO(totalInDb, pendingInRedis); 
    }
    
    // API CẤP LINK CHO BOT CHẠY
    @PostMapping("/add-task")
    public String addTaskToQueue(@RequestParam String url) {
        redisQueueService.pushUrlToQueue(url);
        return "Đã đưa link vào hàng đợi thành công!";
    }
    
    @GetMapping("/domain-stats")
    public ResponseEntity<List<Map<String, Object>>> getDomainStats() {
        String[] domains = {"tuoitre.vn", "thanhnien.vn", "vnexpress.net"};
        Map<String, Integer> failedCounts = queueManager.getFailedCountsByDomain();
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (String domain : domains) {
            Map<String, Object> stat = new HashMap<>();
            long success = articleRepository.countBySourceDomain(domain); // Lấy từ Mongo
            int failed = failedCounts.getOrDefault(domain, 0);            // Lấy từ Redis
            
            stat.put("domain", domain);
            stat.put("success", success);
            stat.put("failed", failed);
            stat.put("total", success + failed);
            result.add(stat);
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentArticles(
    		@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size, 
    		@RequestParam(name = "keyword", defaultValue = "") String keyword, // Thêm dòng này
    		@RequestParam(name = "domain", defaultValue = "") String domain) { // Thêm dòng này

    	// Nếu người dùng chọn "All Domains" trên web, ta biến nó thành chuỗi rỗng để tìm tất cả
        if ("all".equals(domain)) {
            domain = "";
        }
        
     // Gọi hàm tìm kiếm mới
        Page<Article> articlePage = articleRepository
                .findByTitleContainingIgnoreCaseAndSourceDomainContainingIgnoreCaseOrderByCrawledAtDesc(
                        keyword, domain, PageRequest.of(page, size));

        // Đóng gói dữ liệu kèm theo thông tin phân trang để gửi cho Web
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage.getContent());     // Danh sách 10 bài báo
        response.put("currentPage", articlePage.getNumber());   // Trang hiện tại
        response.put("totalItems", articlePage.getTotalElements()); // Tổng số bài trong DB
        response.put("totalPages", articlePage.getTotalPages());    // Tổng số trang

        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/drop-all")
    public ResponseEntity<Void> deleteAllArticles() {
        articleRepository.deleteAll(); // Lệnh này sẽ quét sạch toàn bộ DB
        return ResponseEntity.ok().build();
    }
}