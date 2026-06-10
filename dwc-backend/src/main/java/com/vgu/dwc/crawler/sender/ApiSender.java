package com.vgu.dwc.crawler.sender;

import com.google.gson.Gson;
import com.vgu.dwc.crawler.model.ArticleData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiSender {
    
    // Địa chỉ API đang chạy cổng 8080 
    private static final String API_URL = "http://localhost:8080/api/articles";
    
    // Khởi tạo Client với thời gian chờ tối đa 10 giây (tránh treo máy nếu API sập)
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
            
    private static final Gson gson = new Gson();

    public static boolean sendArticle(ArticleData article) {
        try {
            // Ép khuôn ArticleData thành chuỗi văn bản JSON
            String jsonPayload = gson.toJson(article);

            // Gói bưu kiện HTTP POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json") // Đính kèm nhãn mác JSON
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Gửi bưu kiện đi và chờ kết quả
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Đọc trạng thái trả về từ API
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("✅ Đã gửi thành công lên API: " + article.getUrl());
                return true;
            } else {
                System.out.println("❌ API từ chối (Lỗi " + response.statusCode() + ") - Mã băm MD5 có thể đã bị trùng!");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ Lỗi gọi API: Máy chủ chưa bật hoặc sai đường dẫn? Chi tiết: " + e.getMessage());
            return false;
        }
    }
}