package com.example.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DltConsumer {

    @KafkaListener(
            topics = "${app.kafka.topic.user-login-dlt:user-login-topic-dlt}",
            groupId = "notification-dlt-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDltMessage(
            @Payload Object failedPayload,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_PARTITION, required = false) Integer originalPartition,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_OFFSET, required = false) Long originalOffset,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            Acknowledgment acknowledgment) {

        log.error("""
                ⚠️ [DEAD LETTER QUEUE (DLT) ALERT]
                ├── Original Topic:     {}
                ├── Original Partition: {}
                ├── Original Offset:    {}
                ├── Error Message:      {}
                └── Failed Payload:     {}
                """,
                originalTopic, originalPartition, originalOffset, exceptionMessage, failedPayload);

        // In production: write to an audit / dead-letter table or notify Slack / PagerDuty

        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }
}
