package com.vgu.dwc.api.controller;

import com.vgu.dwc.api.dto.LogMessage;
import com.vgu.dwc.api.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private SystemLogService logService;

    // Web gọi API này để lấy danh sách log
    @GetMapping
    public ResponseEntity<List<LogMessage>> getLogs() {
        return ResponseEntity.ok(logService.getRecentLogs());
    }

    // Bấm nút "Clear" trên Web sẽ gọi API này
    @DeleteMapping
    public ResponseEntity<Void> clearLogs() {
        logService.clearLogs();
        return ResponseEntity.ok().build();
    }
}