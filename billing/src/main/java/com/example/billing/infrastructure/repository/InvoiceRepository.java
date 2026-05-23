package com.example.billing.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends MongoRepository<InvoiceEntity, String> {

    List<InvoiceEntity> findByCustomerId(Long customerId);

    List<InvoiceEntity> findByStatus(String status);
}
