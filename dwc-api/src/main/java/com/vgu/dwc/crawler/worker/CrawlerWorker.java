package com.vgu.dwc.crawler.worker;

import com.vgu.dwc.crawler.model.ArticleData;
import com.vgu.dwc.crawler.parser.BaseParser;
import com.vgu.dwc.crawler.queue.RedisQueueManager;
import com.vgu.dwc.crawler.sender.ApiSender;

import java.util.List;

public class CrawlerWorker implements Runnable {
    private String workerId;
    private List<BaseParser> parsers;
    private RedisQueueManager queue; 
    
    // 1. Biến cờ hiệu để điều khiển việc Dừng/Chạy (volatile để an toàn trong đa luồng)
    private volatile boolean isRunning = true; 
    
    // 2. Biến đếm số bài báo con Bot này đã cào được
    private int crawledCount = 0;

    // Cập nhật Constructor: BỎ targetUrl đi, Bot sẽ tự lấy url từ queue
    public CrawlerWorker(String workerId, List<BaseParser> parsers, RedisQueueManager queue) {
        this.workerId = workerId;
        this.parsers = parsers;
        this.queue = queue;
    }

    @Override
    public void run() {
        System.out.println("🤖 " + workerId + " đã khởi động và đang chờ việc...");
        
        // Vòng lặp sinh tử: Chạy liên tục cho đến khi bị Kill
        while (isRunning) {
            // Thò tay vào Redis lấy link từ WAITING sang PROCESSING
            String targetUrl = queue.pollUrl(); 

            // Nếu Redis đang cạn link, cho Bot ngủ 2 giây rồi tìm lại, tránh nóng CPU
            if (targetUrl == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue; 
            }

            // Nếu có link thì bắt đầu làm việc
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
                    
                    // Cào thành công -> Tăng biến đếm để báo cáo lên Web
                    crawledCount++;
                }
                
                // Cào xong 1 bài, nghỉ ngơi 1.5s để tránh bị block IP
                Thread.sleep(1500);

            } catch (java.net.SocketTimeoutException | java.net.ConnectException e) {
                System.out.println("📶 " + workerId + " rớt mạng tại " + targetUrl + ". Đang gửi lại...");
                queue.requeueUrl(targetUrl);
            } catch (org.jsoup.HttpStatusException e) {
                if (e.getStatusCode() == 404 || e.getStatusCode() == 403) {
                    System.out.println("💀 " + workerId + " gặp Link chết (404/403). Đang cách ly...");
                    queue.pushToFailed(targetUrl);
                    queue.ackUrl(targetUrl);
                } else {
                    System.out.println("🌩️ " + workerId + " máy chủ báo lỗi " + e.getStatusCode() + ". Đang thử lại...");
                    queue.requeueUrl(targetUrl);
                }
            } catch (Exception e) {
                System.out.println("⚠️ " + workerId + " dính lỗi DOM/Logic: " + e.getMessage());
                queue.pushToFailed(targetUrl);
                queue.ackUrl(targetUrl);
            }
        }
        
        System.out.println("🛑 " + workerId + " đã nhận lệnh tắt và dừng an toàn.");
    }

    // ==========================================
    // CÁC HÀM GETTER ĐỂ SPRING BOOT GỌI LẤY SỐ LIỆU
    // ==========================================
    
    // Hàm này dùng để Kill Bot
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