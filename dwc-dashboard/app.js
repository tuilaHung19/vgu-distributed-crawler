// URL gốc của Backend Spring Boot (Thường chạy port 8080)
const API_BASE_URL = 'http://127.0.0.1:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    console.log("DWC Dashboard is ready!");

    // ==========================================
    // 1. TRANG OVERVIEW
    // ==========================================
    if (document.getElementById('queueSize')) {
        initOverview();
    }

    // Các trang khác sẽ thêm vào đây sau...
});

// Hàm khởi tạo cho trang Overview
function initOverview() {
    // Gọi ngay lần đầu tiên khi load trang
    fetchSystemStats();

    // Thiết lập vòng lặp: Cứ 2 giây (2000ms) sẽ tự động gọi lại API để số liệu nhảy Real-time
    setInterval(fetchSystemStats, 2000);
}

// Hàm Fetch dữ liệu từ API của bạn
async function fetchSystemStats() {
    try {
        // Gọi đến API getSystemStats() mà bạn đã viết trong ArticleController.java
        const response = await fetch(`${API_BASE_URL}/articles/stats`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();

        // Cập nhật DOM (Ghi đè số thật lên số giả trong HTML)
        document.getElementById('queueSize').innerText = data.pendingUrls.toLocaleString();
        document.getElementById('crawledCount').innerText = data.totalArticles.toLocaleString();
        
        // Tạm thời để số ảo cho Worker và Speed nếu Backend chưa có API đếm số lượng này
        // Nếu bạn có API rồi thì cứ thay data.activeWorkers vào đây
        document.getElementById('activeBots').innerText = "0"; 
        document.getElementById('totalSpeed').innerText = "0";

    } catch (error) {
        console.error("Lỗi khi lấy dữ liệu Stats:", error);
        // Nếu lỗi (ví dụ Backend chưa bật), có thể hiển thị một dấu gạch ngang
        document.getElementById('queueSize').innerText = "--";
        document.getElementById('crawledCount').innerText = "--";
    }
}