// ==========================================
// CÁC BIẾN TOÀN CỤC CHO SPIDER
// ==========================================
let spiderInterval = null;
let isSpiderRunning = false;
let speedChart = null; 

// KHÔI PHỤC BIẾN NHỚ TỪ STORAGE
let previousTotalArticles = parseInt(sessionStorage.getItem('dwcPrevTotal')) || 0;

document.addEventListener('DOMContentLoaded', () => {
    console.log("DWC Dashboard is ready!");

    const btnAddBot = document.getElementById('btnAddBot');
    if (btnAddBot) btnAddBot.addEventListener('click', addWorker);

    const btnRunSpider = document.getElementById('btnRunSpider');
    if (btnRunSpider) btnRunSpider.addEventListener('click', toggleSpider);

    initChart();  

    fetchSystemStats();
    fetchDomainStats(); 
    fetchWorkers();
    
    setInterval(fetchSystemStats, 2000);
    setInterval(fetchDomainStats, 4000); 
    setInterval(fetchWorkers, 2000);
});

// ==========================================
// 1. KHỞI TẠO VÀ VẼ BIỂU ĐỒ (CÓ LƯU TRỮ LỊCH SỬ ĐỔI TAB)
// ==========================================
function initChart() {
    const ctx = document.getElementById('speedChart');
    if (!ctx) return;

    const savedLabels = JSON.parse(sessionStorage.getItem('dwcChartLabels')) || [];
    const savedData = JSON.parse(sessionStorage.getItem('dwcChartData')) || [];

    speedChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: savedLabels, 
            datasets: [{
                label: 'Tốc độ cào (Links/s)',
                data: savedData, 
                borderColor: '#ef4444', 
                backgroundColor: 'rgba(239, 68, 68, 0.1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4 
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, suggestedMax: 10 }
            },
            animation: { duration: 0 } 
        }
    });
}

function updateChart(speed) {
    if (!speedChart) return;
    const now = new Date().toLocaleTimeString('vi-VN', { hour12: false });
    
    speedChart.data.labels.push(now);
    speedChart.data.datasets[0].data.push(speed);

    // NÂNG GIỚI HẠN: Lưu trữ tới 100 điểm mốc thời gian để cuộn chuột xem lại lịch sử dài hơn
    if (speedChart.data.labels.length > 100) {
        speedChart.data.labels.shift();
        speedChart.data.datasets[0].data.shift();
    }

    const chartWrapper = document.querySelector('.chart-wrapper');
    const chartInner = document.getElementById('chartInner');
    
    if (chartWrapper && chartInner) {
        // [SMART SCROLLING]: Kiểm tra xem người dùng có đang ở sát rìa bên phải đồ thị không (sai số 50px)
        const isScrolledToRight = chartWrapper.scrollWidth - chartWrapper.clientWidth <= chartWrapper.scrollLeft + 50;

        // Mỗi một mốc thời gian trên đồ thị sẽ chiếm khoảng không rộng 60px
        const pointCount = speedChart.data.labels.length;
        const calculatedWidth = Math.max(chartWrapper.clientWidth, pointCount * 60);
        chartInner.style.width = calculatedWidth + 'px';

        // Gọi lệnh render lại đồ thị của Chart.js
        speedChart.update();

        // Áp dụng thuật toán cuộn thông minh giống hệt System Logs
        if (isScrolledToRight) {
            chartWrapper.scrollLeft = chartWrapper.scrollWidth;
        }
    } else {
        speedChart.update();
    }

    // ĐỒNG BỘ LÊN STORAGE
    sessionStorage.setItem('dwcChartLabels', JSON.stringify(speedChart.data.labels));
    sessionStorage.setItem('dwcChartData', JSON.stringify(speedChart.data.datasets[0].data));
}

// ==========================================
// LOGIC: NÚT BẬT/TẮT SPIDER (TRINH SÁT)
// ==========================================
async function toggleSpider() {
    const btn = document.getElementById('btnRunSpider');
    
    if (!isSpiderRunning) {
        isSpiderRunning = true;
        btn.innerHTML = '<i class="fas fa-stop"></i> Stop Spiders';
        btn.style.backgroundColor = '#ef4444'; 
        
        console.log("🚀 Đã khởi động hệ thống Trinh sát tự động!");
        triggerSpiderApi(); 
        spiderInterval = setInterval(triggerSpiderApi, 15000); 
        
    } else {
        isSpiderRunning = false;
        btn.innerHTML = '<i class="fas fa-spider"></i> Run Spiders';
        btn.style.backgroundColor = '#10b981'; 
        
        clearInterval(spiderInterval); 
        console.log("🛑 Đã dừng hệ thống Trinh sát.");
    }
}

async function triggerSpiderApi() {
    try {
        const response = await fetch('http://127.0.0.1:8080/api/workers/run-spiders', { method: 'POST' });
        if (response.ok) {
            console.log("Spider vừa ném thêm link mới vào Redis!");
            fetchSystemStats(); 
        }
    } catch (error) {
        console.error("Lỗi khi chạy Spider:", error);
    }
}

// ==========================================
// LOGIC: WORKER & GIAO DIỆN THÈ (CARD)
// ==========================================
async function fetchSystemStats() {
    try {
        const response = await fetch('http://127.0.0.1:8080/api/articles/stats');
        const data = await response.json();
        
        document.getElementById('queueSize').innerText = data.pendingUrls.toLocaleString();
        document.getElementById('crawledCount').innerText = data.totalArticles.toLocaleString();

        const currentTotal = data.totalArticles;
        let realThroughput = 0;
        
        if (previousTotalArticles > 0 && currentTotal >= previousTotalArticles) {
            const newArticles = currentTotal - previousTotalArticles;
            realThroughput = (newArticles / 2).toFixed(1);
            document.getElementById('totalSpeed').innerText = realThroughput;
        }
        
        previousTotalArticles = currentTotal;
        sessionStorage.setItem('dwcPrevTotal', currentTotal.toString());

        updateChart(realThroughput);

    } catch (error) {}
}

async function addWorker() {
    try {
        const response = await fetch('http://127.0.0.1:8080/api/workers/start', { method: 'POST' });
        if (response.ok) fetchWorkers(); 
    } catch (error) {}
}

async function killWorker(workerId) {
    try {
        const response = await fetch(`http://127.0.0.1:8080/api/workers/${workerId}`, { method: 'DELETE' });
        if (response.ok) fetchWorkers(); 
    } catch (error) {}
}

async function fetchWorkers() {
    try {
        const response = await fetch('http://127.0.0.1:8080/api/workers');
        const workers = await response.json();

        document.getElementById('activeBots').innerText = workers.length;

        const workerGrid = document.getElementById('workerGrid');
        if (!workerGrid) return; 
        
        workerGrid.innerHTML = ''; 

        workers.forEach(worker => {
            workerGrid.innerHTML += `
                <div class="worker-card running">
                    <div class="w-header">
                        <span class="w-name"><i class="fas fa-server"></i> ${worker.workerId}</span>
                        <span class="w-indicator"></span>
                    </div>
                    <div class="w-body">
                        <div class="w-stat"><span>Crawled:</span> <strong>${worker.crawledCount}</strong></div>
                    </div>
                    <button class="btn-kill" onclick="killWorker('${worker.workerId}')">
                        <i class="fas fa-power-off"></i> Kill
                    </button>
                </div>
            `;
        });
    } catch (error) {}
}

async function fetchDomainStats() {
    try {
        const response = await fetch('http://127.0.0.1:8080/api/articles/domain-stats');
        const stats = await response.json();

        const tbody = document.querySelector('.tracked-table tbody');
        if (!tbody) return;

        tbody.innerHTML = ''; 

        stats.forEach(stat => {
            let iconColor = stat.domain.includes('tuoitre') ? 'text-danger' : 
                            stat.domain.includes('thanhnien') ? 'text-primary' : 'text-success';

            tbody.innerHTML += `
                <tr>
                    <td class="domain-name"><i class="fas fa-globe-asia ${iconColor}"></i> ${stat.domain}</td>
                    <td class="time-text">Real-time</td>
                    <td><span class="pill pill-success">${stat.success}</span></td>
                    <td><span class="pill pill-danger">${stat.failed}</span></td>
                    <td class="total-val">${stat.total}</td>
                </tr>
            `;
        });
    } catch (error) {}
}