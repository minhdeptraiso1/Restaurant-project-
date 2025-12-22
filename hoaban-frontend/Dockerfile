# =========================
# 1) BUILD STAGE
# =========================
FROM node:18-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build


# =========================
# 2) RUN STAGE (Nginx)
# =========================
FROM nginx:alpine

# Xoá default config của nginx
RUN rm -rf /usr/share/nginx/html/*

# Copy file build sang thư mục nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copy cấu hình nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
