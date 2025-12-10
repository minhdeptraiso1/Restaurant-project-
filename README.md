# 🌿 Hoa Ban Restaurant – Healthy Dining Reservation & Ordering Platform

Website đặt bàn & đặt món trực tuyến cho **Nhà hàng Hoa Ban** với giao diện hiện đại, realtime notifications, thanh toán trực tuyến và trợ lý AI tư vấn món ăn.

Dự án được phát triển theo kiến trúc **Microservices** gồm:

- **hoaban-frontend** (Vue.js — giao diện người dùng)
- **backend** (Spring Boot — API chính, quản lý nghiệp vụ)
- **backendchat** (Spring Boot — AI Chatbot + Google Gemini)

---

## 📌 1. Giới thiệu tổng quan dự án

### 👨‍🍳 Hệ thống hỗ trợ khách hàng:

- Xem menu món ăn & combo  
- Lọc theo danh mục, giá, tên món  
- Xem chi tiết món ăn (ảnh, calo, mô tả…)  
- Thêm vào giỏ hàng, cập nhật số lượng  
- Đặt món mang về / giao hàng  
- Đặt bàn theo khu vực & khung giờ  
- Xem lịch sử đặt bàn / đặt món  
- Thanh toán **VNPay**  
- Sử dụng **voucher**, tích điểm  
- Tự order món tại bàn bằng **QR Code**  
- Chatbot AI hỗ trợ:
  - tư vấn món theo sở thích (cay / healthy / ít dầu)
  - gợi ý combo theo ngân sách
  - hỗ trợ đặt bàn

### 🧑‍💼 Hệ thống dành cho quản lý & nhân viên:

- Dashboard thống kê doanh thu  
- CRUD:
  - Món ăn
  - Combo
  - Danh mục
  - Voucher
  - Bàn ăn / khu vực  
  - Người dùng  
- Quản lý đơn hàng (xác nhận – xử lý – hoàn tất)  
- Quản lý đặt bàn  
- Gửi email thông báo  
- Duyệt đơn khách 
- Cập nhật trạng thái đơn  
- Xử lý đặt bàn trong ngày  

---

## 📌 2. Kiến trúc dự án
```
root/
│── backend/ # Spring Boot API chính
│── backendchat/ # Spring Boot AI Chat microservice
│── hoaban-frontend/ # Vue.js giao diện
│── docker-compose.yml
│── README.md
```


---

## 📌 3. Công nghệ sử dụng

### 🖥️ Frontend – Vue.js 3
- Vue 3 (Composition API)
- Pinia (State Management)
- Vue Router
- Axios
- TailwindCSS
- Vite

### 🛠 Backend – Spring Boot
- Spring Boot 3  
- Spring Security + JWT  
- JPA / Hibernate  
- PostgreSQL  
- Flyway Migration  
- Spring Mail (Gửi email)  
- Swagger OpenAPI 3  

### 🤖 AI Chat – Gemini API
- Spring AI  
- Google Gemini 2.5 Flash  
- Redis (Lưu lịch sử hội thoại)  

### 💳 Thanh toán VNPay
- Payment Gateway v2.1.0  
- IPN verification  
- Return URL: `http://localhost:5173/vnpay-return`

### 🐳 Deploy / DevOps
- Docker  
- Docker Compose  
- Railway / Render  

---

## 📌 4. Chức năng chi tiết

### 👨‍🍳 Dành cho khách hàng
- Xem menu, combo  
- Lọc theo danh mục, giá, từ khóa  
- Xem chi tiết món ăn  
- Giỏ hàng + cập nhật giỏ hàng  
- Chọn phương thức nhận món: **Tại quầy / Giao hàng**  
- Đặt bàn theo ngày – giờ – khu vực – số người  
- Xem lịch sử đặt bàn / đơn hàng  
- Thanh toán **VNPay**  
- Sử dụng voucher giảm giá  
- Order tại bàn bằng **QR**  
- Chatbot AI:
  - tư vấn món theo sở thích
  - theo ngân sách
  - theo số lượng người
  - hỗ trợ đặt bàn

### 🧑‍💼 Dành cho Quản lý
- Dashboard thống kê doanh thu  
- CRUD món ăn, combo, danh mục, voucher  
- CRUD bàn ăn / khu vực / người dùng  
- Quản lý đơn hàng  
- Quản lý đặt bàn  
- Duyệt đơn khách  
- Gửi email thông báo tự động  
- WebSocket realtime order updates  
- Cập nhật trạng thái đơn  
- Xử lý nhanh đặt bàn theo ca  

---

## 📌 5. Cấu trúc thư mục dự án

### 🎨 Frontend – `hoaban-frontend/src`
```
src/
│── api/
│── assets/
│── components/
│── layouts/
│── pages/
│── router/
│── stores/
│── types/
│── utils/
│── views/
│── App.vue
│── main.ts
```
---

### 🛠 Backend – `backend/src/main/java/com/hoabanrestaurant/backend`
```
│── config/
│── controller/
│── dto/
│── entity/
│── enums/
│── exception/
│── mapper/
│── repository/
│── security/
│── service/
│── util/
│── BackendApplication.java
```

✔ **Cấu hình trong application.yml bao gồm:**  
- PostgreSQL  
- Flyway  
- Spring Mail Gmail  
- Swagger  
- JWT  
- VNPay  
- AI chatbot URL  

---

### 🤖 Backend Chat AI – `backendchat/`
```
│── config/
│── controller/
│── dto/
│── memory/
│── model/
│── nlu/
│── preprocess/
│── security/
│── service/
│── BackendchatApplication.java
```
✔ **application.yml bao gồm:**  
- Gemini AI (model: gemini-2.5-flash)  
- Redis  
- Internal secret  
- Port 8085  

---

## 📌 6. Cách chạy dự án
```
✔ 1️⃣ Chạy Backend


cd backend
./gradlew bootRun
Chạy tại:
➡ http://localhost:8080/api

✔ 2️⃣ Chạy BackendChat (AI)
cd backendchat
./gradlew bootRun
AI chạy tại:
➡ http://localhost:8085/api/ai/chat

✔ 3️⃣ Chạy Frontend
cd hoaban-frontend
npm install
npm run dev
Chạy tại:
➡ http://localhost:5173
```

## 7. Docker Compose
```
Dùng để deploy backend + frontend + PostgreSQL + chatbot AI.


version: "3.8"

services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: hoa_ban
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: *****
    ports:
      - "5432:5432"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - db

  backendchat:
    build: ./backendchat
    ports:
      - "8085:8085"
    depends_on:
      - db

  frontend:
    build: ./hoaban-frontend
    ports:
      - "5173:5173"
```

## 📌 8. Hướng phát triển tương lai
```
Phát triển ứng dụng Mobile (Flutter)

Hệ thống gợi ý món ăn dựa trên Machine Learning

RAG chatbot hiểu toàn bộ menu nhà hàng

Tích hợp ví điện tử: ShopeePay, ZaloPay

Module quản lý kho – nhà cung cấp

Loyalty: tích điểm & xếp hạng thành viên

WebSocket nâng cao
```

## 📌 9. Tác giả
```
Nguyễn Quang Minh
Khoa Công nghệ thông tin – Đại học Kiến trúc Đà Nẵng

```


