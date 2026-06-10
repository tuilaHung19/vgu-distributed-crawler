package com.vgu.dwc.crawler.spider;

import com.vgu.dwc.crawler.queue.RedisQueueManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class TuoiTreHomeSpider {
    private RedisQueueManager queue;

    public TuoiTreHomeSpider(RedisQueueManager queue) {
        this.queue = queue;
    }

    public void crawlHomePage(String seedUrl) {
        try {
            System.out.println("🕷️ Đang quét trang chủ: " + seedUrl);
            // Tải toàn bộ HTML của trang chủ
            Document doc = Jsoup.connect(seedUrl).userAgent("Mozilla/5.0").get();
            
            // Tìm tất cả các thẻ có chứa link
            Elements links = doc.select("a[href]");
            Set<String> uniqueLinks = new HashSet<>();

            for (Element link : links) {
                String articleUrl = link.absUrl("href");
                // Màng lọc: Chỉ lấy link bài báo Tuổi Trẻ (có đuôi .htm)
                if (isArticleUrl(articleUrl)) {
                    uniqueLinks.add(articleUrl);
                }
            }

            // Ném toàn bộ link lọc được vào băng chuyền Redis
            for (String url : uniqueLinks) {
                queue.pushUrl(url);
            }
            System.out.println("✅ Đã tóm được " + uniqueLinks.size() + " link và ném vào Redis!");
            
        } catch (Exception e) {
            System.out.println("❌ Lỗi mạng khi gom link: " + e.getMessage());
        }
    }

    // Hàm kiểm tra xem link có phải là bài báo thật không
    private boolean isArticleUrl(String url) {
        return url.contains("tuoitre.vn") 
                && !url.contains("/video/") 
                && url.matches(".*-[0-9]{8,}\\.(htm|html)$");
    }
}