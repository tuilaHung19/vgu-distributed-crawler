package com.vgu.dwc.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController; // THÊM DÒNG NÀY

import com.vgu.dwc.crawler.queue.RedisQueueManager;

@RestController // THÊM DÒNG NÀY ĐỂ XÓA LỖI HTTP
public class RedisQueueController {
    
    @Autowired
    private RedisQueueManager queueManager;

    @DeleteMapping("/api/queue/purge")
    public ResponseEntity<Void> purgeRedisQueue() {
        queueManager.purgeAll(); 
        return ResponseEntity.ok().build();
    }
}