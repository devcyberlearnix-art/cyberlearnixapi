package com.user.register.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.user-login:user-login-topic}")
    private String userLoginTopic;

    @Bean
    public NewTopic userLoginTopic() {
        return TopicBuilder.name(userLoginTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userLoginDltTopic() {
        return TopicBuilder.name(userLoginTopic + "-dlt")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
