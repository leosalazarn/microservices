package com.example.products.infrastructure.eventstore;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStoreRepository extends MongoRepository<EventStoreEntity, String> {
    List<EventStoreEntity> findByAggregateIdOrderByVersionAsc(String aggregateId);
}
