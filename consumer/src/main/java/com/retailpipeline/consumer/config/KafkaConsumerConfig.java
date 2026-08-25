package com.retailpipeline.consumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConsumerConfig {

  /**
   * Spring Boot's autoconfigured listener container factory picks up any {@code CommonErrorHandler}
   * bean automatically — no need to build a custom container factory just to wire this in.
   */
  @Bean
  public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
    // Publishes to "<topic>.DLT" by default once retries are exhausted.
    var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

    // 500ms, 1s, 2s between attempts, then give up and hand off to the recoverer.
    var backOff = new ExponentialBackOffWithMaxRetries(3);
    backOff.setInitialInterval(500);
    backOff.setMultiplier(2.0);
    backOff.setMaxInterval(5_000);

    return new DefaultErrorHandler(recoverer, backOff);
  }
}
