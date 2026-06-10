package com.vgu.dwc.api.dto;

public class StatsResponseDTO {
    private long totalArticles; // Tổng số bài báo trong DB
    private long pendingUrls;   // Số link đang chờ trong Redis

    // Constructor để nhồi dữ liệu
    public StatsResponseDTO(long totalArticles, long pendingUrls) {
        this.totalArticles = totalArticles;
        this.pendingUrls = pendingUrls;
    }

    // Nhớ tạo Getters và Setters ở đây để Spring Boot có thể chuyển thành JSON nhé!
    public long getTotalArticles() { return totalArticles; }
    public void setTotalArticles(long totalArticles) { this.totalArticles = totalArticles; }
    public long getPendingUrls() { return pendingUrls; }
    public void setPendingUrls(long pendingUrls) { this.pendingUrls = pendingUrls; }
}