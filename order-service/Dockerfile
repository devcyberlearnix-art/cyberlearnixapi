FROM eclipse-temurin:21-jre-alpine

# Set IST timezone
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    echo "Asia/Kolkata" > /etc/timezone

WORKDIR /app

# Copy the built jar file for order-service (update the jar name if needed)
COPY build/libs/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]
