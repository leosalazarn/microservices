package com.example.products.streams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ProductStatsTopology {

    private static final String PRODUCT_EVENTS_TOPIC = "product-events";
    private static final String PRODUCT_STATS_TOPIC = "product-stats";

    @Bean
    public KStream<String, String> productEventsStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder.stream(PRODUCT_EVENTS_TOPIC);
        stream.peek((key, value) -> log.debug("Received event key={}", key));
        return stream;
    }
}
