package com.retailpipeline.consumer.service;

import com.retailpipeline.common.event.OrderCreated;
import com.retailpipeline.common.event.PriceChanged;
import com.retailpipeline.common.event.SalesEvent;
import com.retailpipeline.common.event.StockUpdated;
import com.retailpipeline.consumer.entity.ProcessedEvent;
import com.retailpipeline.consumer.entity.ProductStats;
import com.retailpipeline.consumer.repository.ProcessedEventRepository;
import com.retailpipeline.consumer.repository.ProductStatsRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsAggregationService {

  private static final Logger log = LoggerFactory.getLogger(StatsAggregationService.class);

  private final ProductStatsRepository statsRepository;
  private final ProcessedEventRepository processedEventRepository;

  public StatsAggregationService(
      ProductStatsRepository statsRepository, ProcessedEventRepository processedEventRepository) {
    this.statsRepository = statsRepository;
    this.processedEventRepository = processedEventRepository;
  }

  @Transactional
  public void handle(SalesEvent event) {
    // Kafka is at-least-once: a crash or rebalance between processing an event and
    // committing its offset can redeliver it later. Skip anything we've already
    // applied instead of double-counting it.
    if (processedEventRepository.existsById(event.eventId())) {
      log.info("Skipping already-processed event {}", event.eventId());
      return;
    }

    // Exhaustive over the sealed SalesEvent hierarchy: if a 4th event type is ever
    // added to `common`, this switch fails to compile until a case is added here.
    switch (event) {
      case OrderCreated e -> applyOrderCreated(e);
      case StockUpdated e -> applyStockUpdated(e);
      case PriceChanged e -> applyPriceChanged(e);
    }

    processedEventRepository.save(new ProcessedEvent(event.eventId(), Instant.now()));
  }

  private void applyOrderCreated(OrderCreated e) {
    ProductStats stats = statsFor(e.productId());
    stats.setTotalOrders(stats.getTotalOrders() + 1);
    stats.setTotalQuantitySold(stats.getTotalQuantitySold() + e.quantity());
    BigDecimal orderTotal = e.unitPrice().multiply(BigDecimal.valueOf(e.quantity()));
    stats.setTotalRevenue(stats.getTotalRevenue().add(orderTotal));
    stats.setLastUpdated(e.timestamp());
    statsRepository.save(stats);
  }

  private void applyStockUpdated(StockUpdated e) {
    ProductStats stats = statsFor(e.productId());
    stats.setCurrentStock(e.newQuantity());
    stats.setLastUpdated(e.timestamp());
    statsRepository.save(stats);
  }

  private void applyPriceChanged(PriceChanged e) {
    ProductStats stats = statsFor(e.productId());
    stats.setCurrentPrice(e.newPrice());
    stats.setLastUpdated(e.timestamp());
    statsRepository.save(stats);
  }

  private ProductStats statsFor(String productId) {
    return statsRepository.findById(productId).orElseGet(() -> new ProductStats(productId));
  }
}
