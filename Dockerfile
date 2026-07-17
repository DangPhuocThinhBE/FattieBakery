# Bước 1: Build dự án
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# Vì Docker Context bác đã đặt là fattiebakery, nên lúc này
# file pom.xml đang nằm ngay ở thư mục hiện tại của máy ảo
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy toàn bộ code vào
COPY . .

# Build ra file jar
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# File jar sau khi build xong sẽ nằm trong thư mục /app/target/
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx512M", "-jar", "app.jar"]
