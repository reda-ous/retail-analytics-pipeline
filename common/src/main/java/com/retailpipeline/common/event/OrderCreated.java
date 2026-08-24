package com.retailpipeline.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreated(
    UUID eventId, Instant timestamp, String productId, int quantity, BigDecimal unitPrice)
    implements SalesEvent {}
