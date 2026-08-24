package com.retailpipeline.consumer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_stats")
public class ProductStats {

  @Id private String productId;

  private long totalOrders;
  private long totalQuantitySold;
  private BigDecimal totalRevenue = BigDecimal.ZERO;
  private Integer currentStock;
  private BigDecimal currentPrice;
  private Instant lastUpdated;

  protected ProductStats() {
    // required by Hibernate
  }

  public ProductStats(String productId) {
    this.productId = productId;
  }

  public String getProductId() {
    return productId;
  }

  public long getTotalOrders() {
    return totalOrders;
  }

  public void setTotalOrders(long totalOrders) {
    this.totalOrders = totalOrders;
  }

  public long getTotalQuantitySold() {
    return totalQuantitySold;
  }

  public void setTotalQuantitySold(long totalQuantitySold) {
    this.totalQuantitySold = totalQuantitySold;
  }

  public BigDecimal getTotalRevenue() {
    return totalRevenue;
  }

  public void setTotalRevenue(BigDecimal totalRevenue) {
    this.totalRevenue = totalRevenue;
  }

  public Integer getCurrentStock() {
    return currentStock;
  }

  public void setCurrentStock(Integer currentStock) {
    this.currentStock = currentStock;
  }

  public BigDecimal getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(BigDecimal currentPrice) {
    this.currentPrice = currentPrice;
  }

  public Instant getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Instant lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
}
