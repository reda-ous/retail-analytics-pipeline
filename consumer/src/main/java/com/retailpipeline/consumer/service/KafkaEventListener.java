package com.retailpipeline.consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.retailpipeline.common.event.SalesEvent;

@Component
public class KafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventListener.class);

    private final StatsAggregationService aggregationService;

    public KafkaEventListener(StatsAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @KafkaListener(topics = "${retail.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEvent(SalesEvent event) {
        log.info("Consumed event: {}", event);
        aggregationService.handle(event);
    }
}
