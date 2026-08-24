package com.retailpipeline.consumer.web;

import com.retailpipeline.consumer.entity.ProductStats;
import com.retailpipeline.consumer.repository.ProductStatsRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

  private final ProductStatsRepository statsRepository;

  public StatsController(ProductStatsRepository statsRepository) {
    this.statsRepository = statsRepository;
  }

  @GetMapping("/products")
  public List<ProductStatsResponse> allProducts() {
    return statsRepository.findAll().stream().map(ProductStatsResponse::from).toList();
  }

  @GetMapping("/products/{productId}")
  public ResponseEntity<ProductStatsResponse> product(@PathVariable String productId) {
    return statsRepository
        .findById(productId)
        .map(ProductStatsResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/summary")
  public SummaryResponse summary() {
    List<ProductStats> all = statsRepository.findAll();
    long totalOrders = all.stream().mapToLong(ProductStats::getTotalOrders).sum();
    long totalQuantity = all.stream().mapToLong(ProductStats::getTotalQuantitySold).sum();
    BigDecimal totalRevenue =
        all.stream().map(ProductStats::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new SummaryResponse(all.size(), totalOrders, totalQuantity, totalRevenue);
  }
}
