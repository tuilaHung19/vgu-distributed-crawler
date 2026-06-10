package com.vgu.dwc.crawler.spider;

import com.vgu.dwc.crawler.queue.RedisQueueManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class ThanhNienHomeSpider {
    private RedisQueueManager queue;

    public ThanhNienHomeSpider(RedisQueueManager queue) {
        this.queue = queue;
    }

    public void crawlHomePage(String seedUrl) {
        try {
            System.out.println("🕷️ [ThanhNien] Đang quét trang chủ: " + seedUrl);
            Document doc = Jsoup.connect(seedUrl).userAgent("Mozilla/5.0").get();
            
            Elements links = doc.select("a[href]");
            Set<String> uniqueLinks = new HashSet<>();

            for (Element link : links) {
                String articleUrl = link.absUrl("href");
                // Lọc link chuẩn của Thanh Niên
                if (isArticleUrl(articleUrl)) {
                    uniqueLinks.add(articleUrl);
                }
            }

            for (String url : uniqueLinks) {
                queue.pushUrl(url);
            }
            System.out.println("✅ [ThanhNien] Đã tóm được " + uniqueLinks.size() + " link và ném vào Redis!");
            
        } catch (Exception e) {
            System.out.println("❌ [ThanhNien] Lỗi mạng khi gom link: " + e.getMessage());
        }
    }

    private boolean isArticleUrl(String url) {
        // Lấy link nội bộ Thanh Niên, bỏ qua video, và kết thúc bằng .htm
        return url.contains("thanhnien.vn") 
                && !url.contains("/video/") 
                && url.matches(".*[0-9]{8,}\\.htm$"); // Thanh Niên thường có chuỗi ID số dài ở cuối
    }
}