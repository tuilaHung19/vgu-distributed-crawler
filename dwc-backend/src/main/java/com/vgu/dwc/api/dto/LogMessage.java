package com.vgu.dwc.api.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogMessage {
    private String time;
    private String level;
    private String source;
    private String message;

    public LogMessage(String level, String source, String message) {
        // Tự động đóng dấu thời gian (Ví dụ: 14:30:15.123)
        this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        this.level = level;
        this.source = source;
        this.message = message;
    }

    // Getters
    public String getTime() { return time; }
    public String getLevel() { return level; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
}