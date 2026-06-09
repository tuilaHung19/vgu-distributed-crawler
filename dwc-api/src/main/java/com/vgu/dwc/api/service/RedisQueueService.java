package com.vgu.dwc.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service // Khai báo đây là Tầng Service (Khối óc xử lý logic)
public class RedisQueueService {

    @Autowired
    private StringRedisTemplate redisTemplate; // Công cụ Spring Boot hỗ trợ chơi với Redis

    // Tên của cái ống hàng đợi chứa các Link
    private static final String QUEUE_NAME = "crawler:waiting_list"; 

    // Hàm 1: Nhét link mới vào hàng đợi để Huy cào
    public void pushUrlToQueue(String url) {
        redisTemplate.opsForList().leftPush(QUEUE_NAME, url);
        System.out.println("Đã đẩy link vào Redis cho Bot cào: " + url);
    }

    // Hàm 2: Đếm xem trong ống còn bao nhiêu link chưa cào
    public long getQueueSize() {
        Long size = redisTemplate.opsForList().size(QUEUE_NAME);
        return size != null ? size : 0;
    }
}