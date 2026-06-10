package com.vgu.dwc.crawler.worker;

import com.vgu.dwc.api.service.SystemLogService;
import com.vgu.dwc.crawler.model.ArticleData;
import com.vgu.dwc.crawler.parser.BaseParser;
import com.vgu.dwc.crawler.queue.RedisQueueManager;
import com.vgu.dwc.crawler.sender.ApiSender;
import java.util.Random;
import java.util.List;

public class CrawlerWorker implements Runnable {
    private String workerId;
    private List<BaseParser> parsers;
    private RedisQueueManager queue; 
    
    // 1. Biến cờ hiệu để điều khiển việc Dừng/Chạy
    private volatile boolean isRunning = true; 
    
    // 2. Biến đếm số bài báo con Bot này đã cào được
    private int crawledCount = 0;

    public CrawlerWorker(String workerId, List<BaseParser> parsers, RedisQueueManager queue) {
        this.workerId = workerId;
        this.parsers = parsers;
        this.queue = queue;
    }

    @Override
    public void run() {
        // [LOG INFO] - Bot khởi động
        SystemLogService.log("INFO", workerId, "Worker đã khởi động và đang chờ việc.");
        
        // Vòng lặp sinh tử: Chạy liên tục cho đến khi bị Kill
        while (isRunning) {
            String targetUrl = queue.pollUrl(); 

            // Nếu Redis đang cạn link, cho Bot ngủ 2 giây rồi tìm lại
            if (targetUrl == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue; 
            }

            try {
                ArticleData article = null;
                for (BaseParser p : parsers) {
                    if (p.supports(targetUrl)) {
                        article = p.parse(targetUrl, workerId);
                        break; 
                    }
                }

                if (article == null || article.getTitle().isEmpty() || article.getContent().isEmpty()) {
                    throw new RuntimeException("DOM đổi cấu trúc hoặc không bóc được nội dung!");
                }

                if (ApiSender.sendArticle(article)) {
                    queue.ackUrl(targetUrl);
                    crawledCount++;
                    
                    // [LOG INFO] - Cào và lưu Database thành công
                    // Cắt ngắn tiêu đề nếu quá dài để Log không bị tràn màn hình
                    String shortTitle = article.getTitle().length() > 40 ? article.getTitle().substring(0, 40) + "..." : article.getTitle();
                    SystemLogService.log("INFO", workerId, "Đã lưu thành công bài: [" + shortTitle + "]");
                }
                
                // Tạo độ trễ ngẫu nhiên từ 100 ms đến 300 ms để mô phỏng thực tế
                Random random = new Random();
                int delayTime = 100 + random.nextInt(200); 
                Thread.sleep(delayTime);

            } catch (java.net.SocketTimeoutException | java.net.ConnectException e) {
                // [LOG WARN] - Lỗi mạng nhẹ, có thể cứu vãn
                SystemLogService.log("WARN", workerId, "Rớt mạng/Timeout. Đang trả link lại hàng đợi Redis...");
                queue.requeueUrl(targetUrl);
            } catch (org.jsoup.HttpStatusException e) {
                if (e.getStatusCode() == 404 || e.getStatusCode() == 403) {
                    // [LOG ERROR] - Lỗi nặng, bị chặn IP hoặc link chết
                    SystemLogService.log("ERROR", workerId, "Lỗi HTTP " + e.getStatusCode() + " (Bị chặn/Link chết). Đã chuyển vào thùng rác FAILED_LIST.");
                    queue.pushToFailed(targetUrl);
                    queue.ackUrl(targetUrl);
                } else {
                    // [LOG WARN] - Lỗi server 500, 502, 503...
                    SystemLogService.log("WARN", workerId, "Máy chủ báo lỗi HTTP " + e.getStatusCode() + ". Đang thử lại...");
                    queue.requeueUrl(targetUrl);
                }
            } catch (Exception e) {
                // [LOG ERROR] - Lỗi logic code hoặc DOM HTML tòa soạn bị thay đổi
                SystemLogService.log("ERROR", workerId, "Dính lỗi DOM/Logic: " + e.getMessage() + ". Đã đẩy vào FAILED_LIST.");
                queue.pushToFailed(targetUrl);
                queue.ackUrl(targetUrl);
            }
        }
        
        // [LOG INFO] - Khi bạn bấm nút Kill trên Dashboard
        SystemLogService.log("INFO", workerId, "Đã nhận lệnh Kill và dừng an toàn.");
    }

    public void stopWorker() {
        this.isRunning = false;
    }

    public int getCrawledCount() {
        return crawledCount;
    }

    public String getWorkerId() {
        return workerId;
    }
}