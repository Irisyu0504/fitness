# ===== 构建阶段 =====
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# 先复制 pom.xml，利用 Docker 缓存加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn package -DskipTests -B

# ===== 运行阶段 =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /build/target/fitness_backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
