package com.vgu.dwc.api.controller;

import com.vgu.dwc.api.service.SpiderManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpiderController {

    @Autowired
    private SpiderManagerService spiderManagerService;

    // API nhận link mới từ giao diện Settings
    @PostMapping("/api/spider/add-seed")
    public ResponseEntity<String> addSeedLink(@RequestParam(name = "url") String url) {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("URL không hợp lệ");
        }
        
        // Gọi Service xử lý
        spiderManagerService.addCustomSeedLink(url);
        return ResponseEntity.ok("Đã nạp link vào Spider thành công!");
    }
}