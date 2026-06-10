package com.vgu.dwc.crawler.parser;

import com.vgu.dwc.crawler.model.ArticleData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.security.MessageDigest;

public abstract class BaseParser {

    // 1. TÀI SẢN CHUNG: Hàm tải HTML có bọc sẵn Exponential Backoff
    protected Document fetchDocumentWithRetry(String url) throws IOException {
        int maxRetries = 3; // Thử tối đa 3 lần
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                return Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(15000).get();
            } catch (org.jsoup.HttpStatusException e) {
                if (e.getStatusCode() == 429 || e.getStatusCode() == 503) {
                    attempt++;
                    long waitTime = (long) (Math.pow(2, attempt) * 1000); // 2s -> 4s -> 8s
                    System.out.println("⏳ Bị chặn/Quá tải ở " + url + "! Ngủ " + waitTime + "ms rồi thử lại...");
                    try { Thread.sleep(waitTime); } catch (InterruptedException ie) {}
                } else {
                    throw e; // Lỗi khác như 404 (Not found) thì quăng lỗi luôn
                }
            }
        }
        throw new IOException("❌ Bỏ cuộc! Đã thử 3 lần nhưng mạng vẫn lỗi: " + url);
    }

    // 2. TÀI SẢN CHUNG: Hàm tạo mã băm MD5
    protected String generateMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            return "hash_error_" + System.currentTimeMillis();
        }
    }

    // 3. QUY TẮC BẮT BUỘC: Lớp con thừa kế phải tự viết 2 hàm này
    public abstract boolean supports(String url);
    public abstract ArticleData parse(String url, String workerId) throws IOException;
}