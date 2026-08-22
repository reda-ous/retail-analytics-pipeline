package com.retailpipeline.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PriceChanged(
        UUID eventId,
        Instant timestamp,
        String productId,
        BigDecimal oldPrice,
        BigDecimal newPrice
) implements SalesEvent {
}
