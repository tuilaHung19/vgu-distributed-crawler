package com.vgu.dwc.crawler.queue;

import redis.clients.jedis.UnifiedJedis;

public class RedisQueueManager {
    // Định nghĩa các Key chuẩn theo đúng bản vẽ kiến trúc
    private static final String WAITING_LIST = "crawler:waiting_list";
    private static final String PROCESSING_LIST = "crawler:processing_list";
    private static final String BLOOM_FILTER = "crawler:url_bloom_filter"; 
    private static final String FAILED_LIST = "crawler:failed_list"; // Thùng rác chứa các link bị lỗi
    
    private UnifiedJedis jedis;

    public RedisQueueManager() {
        // Sử dụng UnifiedJedis để giao tiếp mượt mà với Redis Stack (Bloom Filter) & Đa luồng
        jedis = new UnifiedJedis("redis://localhost:6379");
        System.out.println("✅ Đã kết nối Redis Stack (Hỗ trợ Bloom Filter) thành công!");
    }

    // 1. Gieo hạt (Seed) & Lọc trùng
    public void pushUrl(String url) {
        // Áp dụng RedisBloom: Chỉ lấy link chưa từng gặp (Unseen content)
        if (!jedis.bfExists(BLOOM_FILTER, url)) {
            jedis.bfAdd(BLOOM_FILTER, url);  // Đánh dấu là đã thấy
            jedis.lpush(WAITING_LIST, url);  // Đẩy vào hàng chờ
        } else {
            // Seen Already Content: Bỏ qua hoàn toàn
            System.out.println("♻️ [Bloom Filter] Bỏ qua link đã cào: " + url);
        }
    }

    // 2. Worker nhận việc an toàn (Atomic Operation)
    public String pollUrl() {
        // RPOPLPUSH: Rút khỏi WAITING và nhét ngay sang PROCESSING, không bao giờ rơi mất link
        return jedis.rpoplpush(WAITING_LIST, PROCESSING_LIST);
    }

    // 3. Worker báo cáo hoàn thành
    public void ackUrl(String url) {
        // LREM: Xóa vĩnh viễn link khỏi PROCESSING sau khi lưu DB thành công
        jedis.lrem(PROCESSING_LIST, 1, url);
    }

    // 4. Xử lý khi bóc tách/gọi API thất bại
    public void pushToFailed(String url) {
        // Tống link lỗi vào thùng rác để kỹ sư kiểm tra sau, tránh kẹt hệ thống
        jedis.lpush(FAILED_LIST, url);
    }
    
    // Xử lý Lỗi Tạm Thời: Rút từ PROCESSING trả ngược về WAITING
    public void requeueUrl(String url) {
        // 1. Xóa nó khỏi danh sách đang xử lý
        jedis.lrem(PROCESSING_LIST, 1, url);
        // 2. Tống nó lại vào đầu hàng chờ để con Bot khác (hoặc chính nó) bốc lại ngay
        jedis.lpush(WAITING_LIST, url); 
        System.out.println("🔄 [Auto-Retry] Đã trả link về WAITING_LIST do lỗi mạng: " + url);
    }

    // 5. Boot-time Orphaned Task Sweeper: Giải cứu link kẹt
    public void recoverOrphanedTasks() {
        long orphanedCount = jedis.llen(PROCESSING_LIST);
        if (orphanedCount > 0) {
            System.out.println("🚨 Phát hiện " + orphanedCount + " link bị kẹt (Orphaned Tasks)! Đang giải cứu...");
            for (int i = 0; i < orphanedCount; i++) {
                // Đưa các link đang xử lý dở dang (do bot chết) quay lại hàng đợi chờ
                jedis.rpoplpush(PROCESSING_LIST, WAITING_LIST); 
            }
            System.out.println("✅ Đã khôi phục toàn bộ link kẹt về lại WAITING_LIST!");
        }
    }

    // Kiểm tra ống chờ còn việc không
    public boolean isEmpty() {
        return jedis.llen(WAITING_LIST) == 0;
    }
}