package com.retailpipeline.common.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType")
@JsonSubTypes({
  @JsonSubTypes.Type(value = OrderCreated.class, name = "ORDER_CREATED"),
  @JsonSubTypes.Type(value = StockUpdated.class, name = "STOCK_UPDATED"),
  @JsonSubTypes.Type(value = PriceChanged.class, name = "PRICE_CHANGED")
})
public sealed interface SalesEvent permits OrderCreated, StockUpdated, PriceChanged {

  UUID eventId();

  Instant timestamp();

  String productId();
}
