package com.example.demo.repository;

import java.util.List;

import com.example.demo.document.CustomerDocument;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface CustomerRepository extends RedisDocumentRepository<CustomerDocument, String> {

	List<CustomerDocument> findByStatus(String status);
}