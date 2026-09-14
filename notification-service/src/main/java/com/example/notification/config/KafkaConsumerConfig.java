package com.example.notification.config;

import com.cyberlearnix.commonlibs.dto.UserLoginEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:notification-group}")
    private String groupId;

    @Value("${app.kafka.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.kafka.retry.interval-ms:1000}")
    private long retryIntervalMs;

    @Bean
    public ConsumerFactory<String, UserLoginEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // ErrorHandlingDeserializer prevents poison pills from halting the consumer container
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserLoginEvent.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(UserLoginEvent.class, false))
        );
    }

    /**
     * Producer template used by DeadLetterPublishingRecoverer to forward failed records to DLT
     */
    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    /**
     * Error Handler with retry backoff and routing to Dead Letter Topic (<topic>-dlt)
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> dltKafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltKafkaTemplate,
                (record, ex) -> {
                    log.error("Forwarding message to DLT topic {}-dlt due to: {}", record.topic(), ex.getMessage());
                    return new TopicPartition(record.topic() + "-dlt", record.partition());
                }
        );

        FixedBackOff backOff = new FixedBackOff(retryIntervalMs, maxAttempts - 1);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // Non-retryable exceptions (fatal schema/serialization issues)
        handler.addNotRetryableExceptions(
                org.apache.kafka.common.errors.SerializationException.class,
                IllegalArgumentException.class
        );

        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserLoginEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, UserLoginEvent> consumerFactory,
            DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, UserLoginEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        // 3 parallel consumer threads matching the 3 partitions
        factory.setConcurrency(3);

        // Manual Immediate Acknowledgment: offset is committed immediately once business logic calls ack.acknowledge()
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }
}
