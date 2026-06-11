package com.example.products.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ProductStatsTopology {

    private static final String PRODUCT_EVENTS_TOPIC = "product-events";
    private static final String PRODUCT_STATS_TOPIC = "product-stats";
    private static final String STATS_STORE_NAME = "category-stats-store";

    @Bean
    public KTable<String, CategoryStats> productStatsTable(StreamsBuilder streamsBuilder, ObjectMapper objectMapper) {
        JsonSerde<ProductEvent> eventSerde = new JsonSerde<>(ProductEvent.class, objectMapper);
        JsonSerde<CategoryStats> statsSerde = new JsonSerde<>(CategoryStats.class, objectMapper);

        KStream<String, String> rawStream = streamsBuilder.stream(
                PRODUCT_EVENTS_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String())
        );

        KStream<String, ProductEvent> parsed = rawStream
                .filter((key, value) -> value != null)
                .mapValues(value -> {
                    try {
                        return objectMapper.readValue(value, ProductEvent.class);
                    } catch (Exception e) {
                        log.warn("Failed to deserialize product event", e);
                        return null;
                    }
                })
                .filter((key, event) -> event != null && event.getProductId() != null);

        KStream<String, ProductEvent> categoryPriced = parsed
                .filter((key, event) -> {
                    String cat = effectiveCategory(event);
                    Double price = effectivePrice(event);
                    return cat != null && !cat.isBlank() && price != null && price > 0;
                })
                .selectKey((key, event) -> effectiveCategory(event));

        KTable<String, CategoryStats> statsTable = categoryPriced
                .groupByKey(Grouped.with(Serdes.String(), eventSerde))
                .aggregate(
                        CategoryStats::empty,
                        (key, event, stats) -> {
                            double price = effectivePrice(event);
                            if (stats.getCount() == 0) {
                                return CategoryStats.initial(key, price);
                            }
                            return new CategoryStats(
                                    stats.getCategory(),
                                    stats.getCount() + 1,
                                    stats.getTotalPrice() + price,
                                    Math.min(stats.getMinPrice(), price),
                                    Math.max(stats.getMaxPrice(), price)
                            );
                        },
                        Materialized.<String, CategoryStats, KeyValueStore<Bytes, byte[]>>as(STATS_STORE_NAME)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(statsSerde)
                );

        statsTable.toStream().to(
                PRODUCT_STATS_TOPIC,
                Produced.with(Serdes.String(), statsSerde)
        );

        rawStream.peek((key, value) -> log.debug("Received event key={}", key));

        return statsTable;
    }

    static String effectiveCategory(ProductEvent event) {
        return event.getNewCategory() != null ? event.getNewCategory() : event.getCategory();
    }

    static Double effectivePrice(ProductEvent event) {
        return event.getNewPrice() != null ? event.getNewPrice() : event.getPrice();
    }
}
