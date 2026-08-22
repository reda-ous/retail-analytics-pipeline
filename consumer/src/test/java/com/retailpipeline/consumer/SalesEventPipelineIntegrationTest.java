package com.retailpipeline.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailpipeline.common.event.OrderCreated;
import com.retailpipeline.common.event.SalesEvent;
import com.retailpipeline.consumer.web.ProductStatsResponse;

/**
 * Exercises the real pipeline end to end: a raw JSON event is published straight to a
 * Kafka broker running in a Testcontainers container (standing in for the producer),
 * and the assertion polls the REST API until the consumer — backed by a containerized
 * Postgres — has aggregated it. Nothing here is mocked.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SalesEventPipelineIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("retail")
            .withUsername("retail")
            .withPassword("retail");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @LocalServerPort
    private int port;

    // Plain Jackson 2 mapper, unrelated to Spring Boot's own (Jackson 3-based) JSON
    // setup — this test publishes as an external producer would, not as app code.
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void orderCreatedEventIsAggregatedIntoProductStats() throws Exception {
        String productId = "PRD-TEST-%s".formatted(UUID.randomUUID());
        OrderCreated event = new OrderCreated(UUID.randomUUID(), Instant.now(), productId, 4, new BigDecimal("19.99"));

        publish(event);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                restTestClient.get().uri("/api/stats/products/{id}", productId)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(ProductStatsResponse.class)
                        .value(stats -> {
                            assertThat(stats.totalOrders()).isEqualTo(1);
                            assertThat(stats.totalQuantitySold()).isEqualTo(4);
                            assertThat(stats.totalRevenue()).isEqualByComparingTo(new BigDecimal("79.96"));
                        }));
    }

    private void publish(SalesEvent event) throws Exception {
        Map<String, Object> producerProps = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (Producer<String, String> producer = new KafkaProducer<>(producerProps)) {
            producer.send(new ProducerRecord<>("sales-events", event.productId(),
                    objectMapper.writeValueAsString(event))).get(10, TimeUnit.SECONDS);
        }
    }
}
