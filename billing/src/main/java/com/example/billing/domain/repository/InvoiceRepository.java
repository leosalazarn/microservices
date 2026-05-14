package com.example.billing.domain.repository;

import com.example.billing.domain.entity.InvoiceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends MongoRepository<InvoiceEntity, Long> {

    List<InvoiceEntity> findByCustomerId(Long customerId);

    List<InvoiceEntity> findByStatus(String status);
}
