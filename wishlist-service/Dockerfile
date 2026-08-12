# Runtime - Use pre-built JAR
FROM eclipse-temurin:21-jre-alpine

# Set IST timezone
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

WORKDIR /app

# Copy the pre-built jar
COPY wishlist-service/build/libs/*.jar app.jar

EXPOSE 8090

# Performance tuning for Java 21 and Virtual Threads
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Duser.timezone=Asia/Kolkata", \
            "-Dspring.threads.virtual.enabled=true", \
            "-jar", "app.jar"]



