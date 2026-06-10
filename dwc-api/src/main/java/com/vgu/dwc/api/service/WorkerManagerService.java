package com.vgu.dwc.api.service;

import com.vgu.dwc.crawler.parser.BaseParser;
import com.vgu.dwc.crawler.parser.ThanhNienParser;
import com.vgu.dwc.crawler.parser.TuoiTreParser;
import com.vgu.dwc.crawler.parser.VnExpressParser;
import com.vgu.dwc.crawler.queue.RedisQueueManager;
import com.vgu.dwc.crawler.worker.CrawlerWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkerManagerService {

    @Autowired
    private RedisQueueManager queueManager; // Lấy Queue từ Spring Boot

    // Cuốn sổ lưu trữ các công nhân đang cày cuốc (Dùng ConcurrentHashMap để an toàn đa luồng)
    private final Map<String, CrawlerWorker> activeWorkers = new ConcurrentHashMap<>();
    private final Map<String, Thread> workerThreads = new ConcurrentHashMap<>();
    
    private int workerCounter = 0;

    // Chuẩn bị hộp dụng cụ cho Bot (Giống hệt bên MainApp cũ)
    private final List<BaseParser> parsers = Arrays.asList(
            new TuoiTreParser(),
            new VnExpressParser(),
            new ThanhNienParser()
    );

    // ==========================================
    // 1. TÍNH NĂNG: THÊM WORKER (ADD WORKER)
    // ==========================================
    public String startNewWorker() {
        workerCounter++;
        String workerId = "Worker-" + String.format("%02d", workerCounter); // Tạo tên: Worker-01, Worker-02...

        // Tạo công nhân mới
        CrawlerWorker worker = new CrawlerWorker(workerId, parsers, queueManager);
        
        // Giao việc cho công nhân vào một luồng (Thread) độc lập
        Thread thread = new Thread(worker);
        
        // Ghi danh vào sổ
        activeWorkers.put(workerId, worker);
        workerThreads.put(workerId, thread);
        
        // Kích hoạt chạy
        thread.start();
        
        return workerId;
    }

    // ==========================================
    // 2. TÍNH NĂNG: GIẾT WORKER (KILL WORKER)
    // ==========================================
    public boolean killWorker(String workerId) {
        CrawlerWorker worker = activeWorkers.get(workerId);
        if (worker != null) {
            worker.stopWorker(); // Bật cờ isRunning = false để Bot tự dừng an toàn
            activeWorkers.remove(workerId); // Gạch tên khỏi sổ
            workerThreads.remove(workerId);
            return true;
        }
        return false;
    }

    public void killAllWorkers() {
        for (CrawlerWorker worker : activeWorkers.values()) {
            worker.stopWorker();
        }
        activeWorkers.clear();
        workerThreads.clear();
    }

    // ==========================================
    // 3. TÍNH NĂNG: LẤY THỐNG KÊ CHO GIAO DIỆN WEB
    // ==========================================
    public int getActiveWorkerCount() {
        return activeWorkers.size();
    }

    // Trả về danh sách chi tiết các Bot đang chạy (Tên, Số bài đã cào)
    public List<Map<String, Object>> getWorkerStats() {
        List<Map<String, Object>> statsList = new ArrayList<>();
        
        for (CrawlerWorker worker : activeWorkers.values()) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("workerId", worker.getWorkerId());
            stat.put("crawledCount", worker.getCrawledCount());
            stat.put("status", "Running");
            statsList.add(stat);
        }
        return statsList;
    }
}