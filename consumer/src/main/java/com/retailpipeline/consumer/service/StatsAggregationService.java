package com.retailpipeline.consumer.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailpipeline.common.event.OrderCreated;
import com.retailpipeline.common.event.PriceChanged;
import com.retailpipeline.common.event.SalesEvent;
import com.retailpipeline.common.event.StockUpdated;
import com.retailpipeline.consumer.entity.ProductStats;
import com.retailpipeline.consumer.repository.ProductStatsRepository;

@Service
public class StatsAggregationService {

    private final ProductStatsRepository statsRepository;

    public StatsAggregationService(ProductStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Transactional
    public void handle(SalesEvent event) {
        // Exhaustive over the sealed SalesEvent hierarchy: if a 4th event type is ever
        // added to `common`, this switch fails to compile until a case is added here.
        switch (event) {
            case OrderCreated e -> applyOrderCreated(e);
            case StockUpdated e -> applyStockUpdated(e);
            case PriceChanged e -> applyPriceChanged(e);
        }
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
