package com.retailpipeline.consumer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * One row per successfully-applied event, keyed by its eventId. Kafka only guarantees at-least-once
 * delivery, so a crash or rebalance between processing an event and committing its offset can cause
 * the same event to be redelivered later — this table is what lets {@link
 * com.retailpipeline.consumer.service.StatsAggregationService} recognize and skip a duplicate
 * instead of double-counting it.
 */
@Entity
@Table(name = "processed_event")
public class ProcessedEvent {

  @Id private UUID eventId;

  private Instant processedAt;

  protected ProcessedEvent() {
    // required by Hibernate
  }

  public ProcessedEvent(UUID eventId, Instant processedAt) {
    this.eventId = eventId;
    this.processedAt = processedAt;
  }

  public UUID getEventId() {
    return eventId;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }
}
