package com.retailpipeline.consumer.web;

import com.retailpipeline.consumer.entity.ProductStats;
import com.retailpipeline.consumer.repository.ProductStatsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Tag(
    name = "Product stats",
    description = "Aggregated sales stats built from the Kafka event stream")
public class StatsController {

  private final ProductStatsRepository statsRepository;

  public StatsController(ProductStatsRepository statsRepository) {
    this.statsRepository = statsRepository;
  }

  @GetMapping("/products")
  @Operation(
      summary = "List stats for every tracked product",
      description =
          "One row per product that has received at least one OrderCreated, StockUpdated, or"
              + " PriceChanged event so far.")
  public List<ProductStatsResponse> allProducts() {
    return statsRepository.findAll().stream().map(ProductStatsResponse::from).toList();
  }

  @GetMapping("/products/{productId}")
  @Operation(
      summary = "Get stats for a single product",
      description = "Returns 404 if the product has never appeared in an event.")
  public ResponseEntity<ProductStatsResponse> product(
      @Parameter(description = "Product id, e.g. PRD-0001") @PathVariable String productId) {
    return statsRepository
        .findById(productId)
        .map(ProductStatsResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/summary")
  @Operation(
      summary = "Aggregate summary across all tracked products",
      description = "Sum of orders, quantity sold, and revenue across every product.")
  public SummaryResponse summary() {
    List<ProductStats> all = statsRepository.findAll();
    long totalOrders = all.stream().mapToLong(ProductStats::getTotalOrders).sum();
    long totalQuantity = all.stream().mapToLong(ProductStats::getTotalQuantitySold).sum();
    BigDecimal totalRevenue =
        all.stream().map(ProductStats::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new SummaryResponse(all.size(), totalOrders, totalQuantity, totalRevenue);
  }
}
