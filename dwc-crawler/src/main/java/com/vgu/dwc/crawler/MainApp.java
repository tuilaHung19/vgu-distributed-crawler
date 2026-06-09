package com.vgu.dwc.crawler;

import com.vgu.dwc.crawler.parser.BaseParser;
import com.vgu.dwc.crawler.parser.ThanhNienParser;
import com.vgu.dwc.crawler.parser.TuoiTreParser;
import com.vgu.dwc.crawler.parser.VnExpressParser;
import com.vgu.dwc.crawler.queue.RedisQueueManager;
import com.vgu.dwc.crawler.spider.ThanhNienHomeSpider;
import com.vgu.dwc.crawler.spider.TuoiTreHomeSpider;
import com.vgu.dwc.crawler.spider.VnExpressHomeSpider;
import com.vgu.dwc.crawler.worker.CrawlerWorker;

import java.util.Arrays;
import java.util.List;

public class MainApp {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("🚀 Khởi động Hệ thống Distributed Web Crawler (DWC) Phiên bản Đa nguồn...");

        // 1. Bật trạm trung chuyển Redis (Đảm bảo Docker chứa Redis Stack đang chạy)
        RedisQueueManager queue = new RedisQueueManager();
        
        // Watchdog: Kiểm tra và giải cứu các link bị kẹt từ lần chạy trước
        System.out.println("🛠️ [Watchdog] Đang kiểm tra tàn dư từ lần sập nguồn trước...");
        queue.recoverOrphanedTasks();

        // 2. Thả đội Nhện (Spiders) đi gom link từ 3 trang báo lớn
        System.out.println("🕸️ Đang thả đội Spider đi gom link...");
        
        TuoiTreHomeSpider tuoiTreSpider = new TuoiTreHomeSpider(queue);
        tuoiTreSpider.crawlHomePage("https://tuoitre.vn/");

        VnExpressHomeSpider vnExpressSpider = new VnExpressHomeSpider(queue);
        vnExpressSpider.crawlHomePage("https://vnexpress.net/");
        
        ThanhNienHomeSpider thanhNienSpider = new ThanhNienHomeSpider(queue);
        thanhNienSpider.crawlHomePage("https://thanhnien.vn/");

        // 3. Chuẩn bị HỘP DỤNG CỤ (Gồm 3 con dao mổ cho 3 loại báo)
        System.out.println("🧰 Đang chuẩn bị hộp công cụ bóc tách (Strategy Pattern)...");
        List<BaseParser> allParsers = Arrays.asList(
                new TuoiTreParser(),
                new VnExpressParser(),
                new ThanhNienParser()
        );

        // 4. Triệu hồi đội công nhân bóc tách (Crawler Workers)
        System.out.println("👷 Bắt đầu triệu hồi công nhân xử lý...");
        int workerCount = 1;

        // Vòng lặp: Rút link từ Redis ra để xử lý
        while (!queue.isEmpty()) {
            // RPOPLPUSH: Lấy link từ WAITING sang PROCESSING
            String url = queue.pollUrl();
            
            if (url != null) {
                String workerName = "bot_" + workerCount;

                // Truyền thêm 'queue' vào để Worker biết đường gọi hàm ACK xóa link hoặc đẩy thùng rác
                CrawlerWorker worker = new CrawlerWorker(workerName, allParsers, url, queue);

                // KÍCH HOẠT ĐA LUỒNG: Mỗi con bot sẽ chạy độc lập song song với nhau
                Thread thread = new Thread(worker);
                thread.start();

                workerCount++;
                
                // Bot nghỉ 1 giây trước khi thả con tiếp theo để tránh bị báo chặn IP
                Thread.sleep(1000); 
            }
        }
        
        System.out.println("🏁 Hệ thống đã phân phát hết việc hiện có trong hàng đợi Redis!");
    }
}