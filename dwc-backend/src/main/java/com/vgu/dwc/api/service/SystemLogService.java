package com.vgu.dwc.api.service;

import com.vgu.dwc.api.dto.LogMessage;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class SystemLogService {
    // Dùng Queue để nếu quá 500 dòng thì tự động xóa dòng cũ nhất
    private static final Queue<LogMessage> logs = new ConcurrentLinkedQueue<>();
    private static final int MAX_LOGS = 500;

    // HÀM QUAN TRỌNG: Các file khác sẽ gọi hàm này để gửi Log
    public static void log(String level, String source, String message) {
        if (logs.size() >= MAX_LOGS) {
            logs.poll(); 
        }
        logs.add(new LogMessage(level, source, message));
        
        // Vẫn in ra màn hình Console của Eclipse cho bạn dễ nhìn
        System.out.println("[" + level + "] [" + source + "] " + message); 
    }

    public List<LogMessage> getRecentLogs() {
        return new ArrayList<>(logs);
    }

    public void clearLogs() {
        logs.clear();
    }
}