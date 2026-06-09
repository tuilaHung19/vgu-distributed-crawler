package com.vgu.dwc.crawler.spider;

import com.vgu.dwc.crawler.queue.RedisQueueManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class VnExpressHomeSpider {
    private RedisQueueManager queue;

    public VnExpressHomeSpider(RedisQueueManager queue) {
        this.queue = queue;
    }

    public void crawlHomePage(String seedUrl) {
        try {
            System.out.println("🕷️ [VnExpress] Đang quét trang chủ: " + seedUrl);
            Document doc = Jsoup.connect(seedUrl).userAgent("Mozilla/5.0").get();
            
            Elements links = doc.select("a[href]");
            Set<String> uniqueLinks = new HashSet<>();

            for (Element link : links) {
                String articleUrl = link.absUrl("href");
                // Lọc link chuẩn của VnExpress
                if (isArticleUrl(articleUrl)) {
                    uniqueLinks.add(articleUrl);
                }
            }

            for (String url : uniqueLinks) {
                queue.pushUrl(url);
            }
            System.out.println("✅ [VnExpress] Đã tóm được " + uniqueLinks.size() + " link và ném vào Redis!");
            
        } catch (Exception e) {
            System.out.println("❌ [VnExpress] Lỗi mạng khi gom link: " + e.getMessage());
        }
    }

    private boolean isArticleUrl(String url) {
        // Lấy link nội bộ VnExpress, bỏ qua video/podcast, và phải kết thúc bằng .html
        return url.contains("vnexpress.net") 
                && !url.contains("/video/") 
                && !url.contains("/podcast/")
                && url.matches(".*-[0-9]{7,}\\.html$"); // VnExpress thường có 7 số ID ở cuối link
    }
}