# vgu-distributed-crawler

vgu-distributed-crawler/ (Thư mục gốc của toàn dự án)
│
├── .gitignore               # Đã có. Bổ sung thêm: node_modules, .idea, *.jar...
├── docker-compose.yml       # Đã có. Tuyệt tác của DevOps Lead.
├── README.md                # File giới thiệu dự án (bạn QA/Tech Writer phụ trách)
│
├── backend-crawler/         # THƯ MỤC 1: Dành cho Core Crawler & Data Engineer (Java)
│   ├── src/                 # Chứa toàn bộ source code Java
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── crawler/     # Code JSoup của Core Crawler Developer
│   │   │   │   ├── queue/       # Code RedisQueueManager của Data Engineer
│   │   │   │   ├── db/          # Code MongoManager của Data Engineer
│   │   │   │   └── utils/       # Chứa ThreadManager, BloomFilterService...
│   │   │   └── resources/
│   │   │       └── user_agents.txt # Danh sách User-Agents
│   ├── pom.xml              # (Nếu dùng Maven) File quản lý thư viện (Jedis, JSoup, MongoDB Driver)
│   └── Dockerfile           # (DevOps Lead sẽ viết cái này sau để đóng gói cục code Java này)
│
├── frontend-dashboard/      # THƯ MỤC 2: Dành cho Frontend/Dashboard Developer (HTML/JS)
│   ├── index.html           # Giao diện chính
│   ├── css/
│   │   └── style.css        # Code CSS trang trí
│   ├── js/
│   │   └── app.js           # Logic Fetch API, vẽ Chart.js, xuất CSV
│   └── README.md            # Note về API Contract cho Backend biết
│
├── data-processing/         # THƯ MỤC 3: Dành cho QA & Data Reliability Engineer (Python/Java)
│   ├── scripts/             # Chứa script dọn rác HTML, gắn Tag tự động
│   ├── chaos-tests/         # Chứa script bắn request làm sập hệ thống (Stress Test)
│   └── docs/                # Chứa bản nháp Report, Data Schema Contract
│
└── architecture/            # THƯ MỤC 4: Dành cho System Lead (Chứa bản vẽ kiến trúc)
    └── system_flow.png      # Bản vẽ Draw.io siêu nét
