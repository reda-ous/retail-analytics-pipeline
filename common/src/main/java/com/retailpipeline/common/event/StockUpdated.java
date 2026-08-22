package com.retailpipeline.common.event;

import java.time.Instant;
import java.util.UUID;

public record StockUpdated(
        UUID eventId,
        Instant timestamp,
        String productId,
        int newQuantity
) implements SalesEvent {
}
