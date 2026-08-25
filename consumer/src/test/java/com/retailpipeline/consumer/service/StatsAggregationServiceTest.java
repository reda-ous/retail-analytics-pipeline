package com.retailpipeline.consumer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.retailpipeline.common.event.OrderCreated;
import com.retailpipeline.common.event.PriceChanged;
import com.retailpipeline.common.event.StockUpdated;
import com.retailpipeline.consumer.entity.ProductStats;
import com.retailpipeline.consumer.repository.ProcessedEventRepository;
import com.retailpipeline.consumer.repository.ProductStatsRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Fast, mocked-repository tests for the aggregation math itself — complements {@link
 * com.retailpipeline.consumer.SalesEventPipelineIntegrationTest}, which proves the pipeline is
 * wired correctly end to end but only exercises one event, not the accumulation logic across
 * repeated events for the same product.
 */
@ExtendWith(MockitoExtension.class)
class StatsAggregationServiceTest {

  @Mock private ProductStatsRepository statsRepository;
  @Mock private ProcessedEventRepository processedEventRepository;

  private StatsAggregationService service;

  @BeforeEach
  void setUp() {
    service = new StatsAggregationService(statsRepository, processedEventRepository);
    when(processedEventRepository.existsById(any())).thenReturn(false);
  }

  @Test
  void orderCreatedOnNewProduct_createsStatsWithAccumulatedValues() {
    when(statsRepository.findById("PRD-0001")).thenReturn(Optional.empty());

    OrderCreated event =
        new OrderCreated(UUID.randomUUID(), Instant.now(), "PRD-0001", 3, new BigDecimal("10.00"));

    service.handle(event);

    ProductStats saved = capturedSave();
    assertThat(saved.getProductId()).isEqualTo("PRD-0001");
    assertThat(saved.getTotalOrders()).isEqualTo(1);
    assertThat(saved.getTotalQuantitySold()).isEqualTo(3);
    assertThat(saved.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("30.00"));
    assertThat(saved.getLastUpdated()).isEqualTo(event.timestamp());
  }

  @Test
  void orderCreatedOnExistingProduct_accumulatesOntoPriorTotalsInsteadOfOverwriting() {
    ProductStats existing = new ProductStats("PRD-0001");
    existing.setTotalOrders(5);
    existing.setTotalQuantitySold(20);
    existing.setTotalRevenue(new BigDecimal("100.00"));
    when(statsRepository.findById("PRD-0001")).thenReturn(Optional.of(existing));

    OrderCreated event =
        new OrderCreated(UUID.randomUUID(), Instant.now(), "PRD-0001", 2, new BigDecimal("15.00"));

    service.handle(event);

    ProductStats saved = capturedSave();
    assertThat(saved.getTotalOrders()).isEqualTo(6);
    assertThat(saved.getTotalQuantitySold()).isEqualTo(22);
    assertThat(saved.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("130.00"));
  }

  @Test
  void stockUpdated_setsCurrentStock() {
    when(statsRepository.findById("PRD-0002")).thenReturn(Optional.empty());

    StockUpdated event = new StockUpdated(UUID.randomUUID(), Instant.now(), "PRD-0002", 42);

    service.handle(event);

    assertThat(capturedSave().getCurrentStock()).isEqualTo(42);
  }

  @Test
  void priceChanged_setsCurrentPrice() {
    when(statsRepository.findById("PRD-0003")).thenReturn(Optional.empty());

    PriceChanged event =
        new PriceChanged(
            UUID.randomUUID(),
            Instant.now(),
            "PRD-0003",
            new BigDecimal("9.99"),
            new BigDecimal("12.99"));

    service.handle(event);

    assertThat(capturedSave().getCurrentPrice()).isEqualByComparingTo(new BigDecimal("12.99"));
  }

  @Test
  void duplicateEvent_isSkippedInsteadOfDoubleCounted() {
    OrderCreated event =
        new OrderCreated(UUID.randomUUID(), Instant.now(), "PRD-0001", 3, new BigDecimal("10.00"));
    when(processedEventRepository.existsById(event.eventId())).thenReturn(true);

    service.handle(event);

    verify(statsRepository, never()).save(any());
    verify(processedEventRepository, never()).save(any());
  }

  private ProductStats capturedSave() {
    ArgumentCaptor<ProductStats> captor = ArgumentCaptor.forClass(ProductStats.class);
    verify(statsRepository).save(captor.capture());
    return captor.getValue();
  }
}
