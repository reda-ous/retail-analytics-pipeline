package com.retailpipeline.consumer.web;

import java.math.BigDecimal;

public record SummaryResponse(
        int trackedProducts,
        long totalOrders,
        long totalQuantitySold,
        BigDecimal totalRevenue
) {
}
