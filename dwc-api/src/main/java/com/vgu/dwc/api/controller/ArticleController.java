package com.vgu.dwc.api.controller;

import com.vgu.dwc.api.model.Article;
import com.vgu.dwc.api.repository.ArticleRepository;
import com.vgu.dwc.api.dto.StatsResponseDTO;
import com.vgu.dwc.api.service.RedisQueueService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController // Khai báo đây là một API Server trả về JSON
@RequestMapping("/api/articles") // Đường dẫn gốc
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository; // Gọi cái "Cần cẩu" DB ra để dùng
    
    @Autowired
    private RedisQueueService redisQueueService; // Gọi anh Service vào làm việc

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
}