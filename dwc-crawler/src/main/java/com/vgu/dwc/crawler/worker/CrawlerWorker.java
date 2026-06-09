package com.vgu.dwc.crawler.worker;

import com.vgu.dwc.crawler.model.ArticleData;
import com.vgu.dwc.crawler.parser.BaseParser;
import com.vgu.dwc.crawler.queue.RedisQueueManager;
import com.vgu.dwc.crawler.sender.ApiSender;

import java.util.List;

public class CrawlerWorker implements Runnable {
    private String workerId;
    private List<BaseParser> parsers;
    private String targetUrl;
    private RedisQueueManager queue; // Thêm biến này

    // Cập nhật Constructor
    public CrawlerWorker(String workerId, List<BaseParser> parsers, String targetUrl, RedisQueueManager queue) {
        this.workerId = workerId;
        this.parsers = parsers;
        this.targetUrl = targetUrl;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            ArticleData article = null;
            for (BaseParser p : parsers) {
                if (p.supports(targetUrl)) {
                    article = p.parse(targetUrl, workerId);
                    break; 
                }
            }

            // Kiểm tra xem parse có ra dữ liệu rỗng (do lỗi DOM) không
            if (article == null || article.getTitle().isEmpty() || article.getContent().isEmpty()) {
                throw new RuntimeException("DOM đổi cấu trúc hoặc không bóc được nội dung!");
            }

            // Nếu ngon lành thì gửi API và ACK
            if (ApiSender.sendArticle(article)) {
                queue.ackUrl(targetUrl); // Xóa khỏi Processing
            }
            
        } catch (java.net.SocketTimeoutException | java.net.ConnectException e) {
            // ---------------------------------------------------------
            // BỆNH 1: Đứt cáp quang, rớt Wifi, Server báo quá tải từ chối kết nối
            // CÁCH CHỮA: Lỗi tạm thời -> Trả lại hàng chờ
            // ---------------------------------------------------------
            System.out.println("📶 " + workerId + " rớt mạng tại " + targetUrl + ". Đang gửi lại hàng chờ...");
            queue.requeueUrl(targetUrl);

        } catch (org.jsoup.HttpStatusException e) {
            // ---------------------------------------------------------
            // BỆNH 2: Tòa soạn trả về mã lỗi HTTP cụ thể
            // ---------------------------------------------------------
            if (e.getStatusCode() == 404 || e.getStatusCode() == 403) {
                // 404 (Xóa bài), 403 (Cấm truy cập vĩnh viễn) -> Cách ly
                System.out.println("💀 " + workerId + " gặp Link chết (404/403). Đang cách ly...");
                queue.pushToFailed(targetUrl);
                queue.ackUrl(targetUrl); // Xóa khỏi Processing
            } else {
                // Các lỗi HTTP khác (như 500, 502, 503) thường do máy chủ báo tạm thời sập -> Trả lại hàng chờ
                System.out.println("🌩️ " + workerId + " máy chủ báo lỗi " + e.getStatusCode() + ". Đang thử lại...");
                queue.requeueUrl(targetUrl);
            }

        } catch (Exception e) {
            // ---------------------------------------------------------
            // BỆNH 3: Bóc tách DOM thất bại (NullPointer) hoặc các lỗi rác khác
            // CÁCH CHỮA: Lỗi vĩnh viễn -> Cách ly vào thùng rác
            // ---------------------------------------------------------
            System.out.println("⚠️ " + workerId + " dính lỗi DOM/Logic: " + e.getMessage());
            queue.pushToFailed(targetUrl);
            queue.ackUrl(targetUrl);
        }
    }
}