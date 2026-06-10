package com.vgu.dwc.api.service;

import com.vgu.dwc.crawler.queue.RedisQueueManager;
import com.vgu.dwc.crawler.spider.ThanhNienHomeSpider;
import com.vgu.dwc.crawler.spider.TuoiTreHomeSpider;
import com.vgu.dwc.crawler.spider.VnExpressHomeSpider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpiderManagerService {

    @Autowired
    private RedisQueueManager queueManager; 

    public String runSpiders() {
        try {
            System.out.println("🕷️ Đang thả đội Spiders đi gom link...");

            TuoiTreHomeSpider tuoiTreSpider = new TuoiTreHomeSpider(queueManager);
            ThanhNienHomeSpider thanhNienSpider = new ThanhNienHomeSpider(queueManager);
            VnExpressHomeSpider vnExpressSpider = new VnExpressHomeSpider(queueManager);

            System.out.println("Bắt đầu quét Tuổi Trẻ...");
            tuoiTreSpider.crawlHomePage("https://tuoitre.vn/");
            tuoiTreSpider.crawlHomePage("https://tuoitre.vn/kinh-doanh.htm");
            tuoiTreSpider.crawlHomePage("https://tuoitre.vn/the-thao.htm");
            tuoiTreSpider.crawlHomePage("https://tuoitre.vn/suc-khoe.htm");

            System.out.println("Bắt đầu quét Thanh Niên...");
            thanhNienSpider.crawlHomePage("https://thanhnien.vn/");
            thanhNienSpider.crawlHomePage("https://thanhnien.vn/kinh-te.htm");
            thanhNienSpider.crawlHomePage("https://thanhnien.vn/the-thao.htm");
            thanhNienSpider.crawlHomePage("https://thanhnien.vn/suc-khoe.htm");
            thanhNienSpider.crawlHomePage("https://thanhnien.vn/the-gioi.htm");

            System.out.println("Bắt đầu quét VnExpress...");
            vnExpressSpider.crawlHomePage("https://vnexpress.net/");
            vnExpressSpider.crawlHomePage("https://vnexpress.net/kinh-doanh");
            vnExpressSpider.crawlHomePage("https://vnexpress.net/the-thao");
            vnExpressSpider.crawlHomePage("https://vnexpress.net/suc-khoe");
            vnExpressSpider.crawlHomePage("https://vnexpress.net/the-gioi");

            return "Trinh sát báo cáo: Đội Spiders đã quét thành công các trang chủ và ném link vào Redis!";
            
        } catch (Exception e) {
            System.out.println("Lỗi khi chạy Spiders: " + e.getMessage());
            return "Trinh sát gặp nạn: " + e.getMessage();
        }
    }

    // ĐÃ SỬA LỖI: Tạo mới Spider ngay khi nhận được link
    public void addCustomSeedLink(String url) {
        SystemLogService.log("INFO", "Master-Node", "🎯 Nhận yêu cầu quét chuyên mục mới: " + url);
        
        if (url.contains("tuoitre.vn")) {
            TuoiTreHomeSpider spider = new TuoiTreHomeSpider(queueManager);
            spider.crawlHomePage(url);
        } else if (url.contains("thanhnien.vn")) {
            ThanhNienHomeSpider spider = new ThanhNienHomeSpider(queueManager);
            spider.crawlHomePage(url);
        } else if (url.contains("vnexpress.net")) {
            VnExpressHomeSpider spider = new VnExpressHomeSpider(queueManager);
            spider.crawlHomePage(url);
        } else {
            SystemLogService.log("WARN", "Master-Node", "⚠️ Hệ thống chưa hỗ trợ cào dữ liệu từ tên miền này!");
        }
    }   
}