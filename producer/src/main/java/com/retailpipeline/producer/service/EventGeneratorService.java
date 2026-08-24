package com.retailpipeline.producer.service;

import com.retailpipeline.common.event.OrderCreated;
import com.retailpipeline.common.event.PriceChanged;
import com.retailpipeline.common.event.SalesEvent;
import com.retailpipeline.common.event.StockUpdated;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EventGeneratorService {

  private static final Logger log = LoggerFactory.getLogger(EventGeneratorService.class);

  private static final String[] PRODUCT_IDS = {"PRD-0001", "PRD-0002", "PRD-0003"};

  private enum EventKind {
    ORDER_CREATED,
    STOCK_UPDATED,
    PRICE_CHANGED
  }

  private final KafkaTemplate<String, SalesEvent> kafkaTemplate;
  private final String topic;

  public EventGeneratorService(
      KafkaTemplate<String, SalesEvent> kafkaTemplate,
      @Value("${retail.kafka.topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  @Scheduled(fixedRate = 2000)
  public void generateEvent() {
    String productId = PRODUCT_IDS[ThreadLocalRandom.current().nextInt(PRODUCT_IDS.length)];
    EventKind kind =
        EventKind.values()[ThreadLocalRandom.current().nextInt(EventKind.values().length)];

    SalesEvent event =
        switch (kind) {
          case ORDER_CREATED ->
              new OrderCreated(
                  UUID.randomUUID(),
                  Instant.now(),
                  productId,
                  ThreadLocalRandom.current().nextInt(1, 10),
                  BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(5, 200)));
          case STOCK_UPDATED ->
              new StockUpdated(
                  UUID.randomUUID(),
                  Instant.now(),
                  productId,
                  ThreadLocalRandom.current().nextInt(0, 500));
          case PRICE_CHANGED ->
              new PriceChanged(
                  UUID.randomUUID(),
                  Instant.now(),
                  productId,
                  BigDecimal.valueOf(19.99),
                  BigDecimal.valueOf(24.99));
        };

    kafkaTemplate.send(topic, event.productId(), event);
    log.info("Published event: {}", event);
  }
}
