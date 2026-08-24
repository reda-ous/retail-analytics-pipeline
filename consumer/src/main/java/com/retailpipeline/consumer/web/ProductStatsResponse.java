package com.retailpipeline.consumer.web;

import com.retailpipeline.consumer.entity.ProductStats;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductStatsResponse(
    String productId,
    long totalOrders,
    long totalQuantitySold,
    BigDecimal totalRevenue,
    Integer currentStock,
    BigDecimal currentPrice,
    Instant lastUpdated) {
  public static ProductStatsResponse from(ProductStats stats) {
    return new ProductStatsResponse(
        stats.getProductId(),
        stats.getTotalOrders(),
        stats.getTotalQuantitySold(),
        stats.getTotalRevenue(),
        stats.getCurrentStock(),
        stats.getCurrentPrice(),
        stats.getLastUpdated());
  }
}
