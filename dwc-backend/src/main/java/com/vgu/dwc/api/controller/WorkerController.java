package com.vgu.dwc.api.controller;

import com.vgu.dwc.api.service.SpiderManagerService;
import com.vgu.dwc.api.service.WorkerManagerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    @Autowired
    private WorkerManagerService workerManagerService;
    
    @Autowired
    private SpiderManagerService spiderManagerService;

    // API 1: Lấy danh sách các Bot đang chạy để vẽ lên UI
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getActiveWorkers() {
        return ResponseEntity.ok(workerManagerService.getWorkerStats());
    }

    // API 2: Bấm nút Add Worker trên web sẽ gọi vào đây
    @PostMapping("/start")
    public ResponseEntity<String> startWorker() {
        String workerId = workerManagerService.startNewWorker();
        return ResponseEntity.ok("Đã khởi động thành công: " + workerId);
    }

    // API 3: Bấm nút Kill trên web sẽ gọi vào đây (Truyền ID của Bot vào)
    @DeleteMapping("/{workerId}")
    public ResponseEntity<String> killWorker(@PathVariable("workerId") String workerId) {
        boolean isKilled = workerManagerService.killWorker(workerId);
        if (isKilled) {
            return ResponseEntity.ok("Đã tiêu diệt " + workerId);
        } else {
            return ResponseEntity.badRequest().body("Không tìm thấy " + workerId);
        }
    }

    // API 4: Bấm nút Kill All trong mục Settings
    @PostMapping("/kill-all")
    public ResponseEntity<String> killAllWorkers() {
        workerManagerService.killAllWorkers();
        return ResponseEntity.ok("Đã dọn dẹp toàn bộ hệ thống Worker!");
    }
    
 // API này sẽ hứng sự kiện khi bạn bấm nút "Run Spiders" trên Web
    @PostMapping("/run-spiders")
    public ResponseEntity<String> triggerSpiders() {
        // Gọi hàm thả Spider đi cào
        String result = spiderManagerService.runSpiders();
        
        // Trả kết quả về cho Web
        return ResponseEntity.ok(result);
    }
}