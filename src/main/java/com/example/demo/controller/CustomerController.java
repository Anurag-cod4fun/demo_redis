package com.example.demo.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.document.CustomerDocument;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.cache.RedisWriteBehindCacheClient;
import com.redis.om.spring.repository.support.SimpleRedisDocumentRepository;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private final CustomerRepository customerRepository;
	private final RedisWriteBehindCacheClient cacheClient;

	public CustomerController(CustomerRepository customerRepository, RedisWriteBehindCacheClient cacheClient) {
		this.customerRepository = customerRepository;
		this.cacheClient = cacheClient;
	}

	@PostMapping("/create")
	public ResponseEntity<CustomerDocument> create(@RequestBody CustomerDocument customer) {
		if (customer.getId() == null || customer.getId().isBlank()) {
			customer.setId(UUID.randomUUID().toString());
		}
		SimpleRedisDocumentRepository<CustomerDocument, ?> repo = cacheClient.getRepo(CustomerDocument.class);
		CustomerDocument created = cacheClient.create(repo, customer);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PostMapping("/list")
	public List<CustomerDocument> findAll() {
		return StreamSupport.stream(customerRepository.findAll().spliterator(), false).toList();
	}

	@PostMapping("/get")
	public ResponseEntity<CustomerDocument> findById(@RequestBody Map<String, String> request) {
		String id = request.get("id");
		return customerRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/search")
	public List<CustomerDocument> findByStatus(@RequestBody Map<String, String> request) {
		String status = request.get("status");
		return customerRepository.findByStatus(status);
	}

	@PostMapping("/update")
	public ResponseEntity<CustomerDocument> update(@RequestBody CustomerDocument customer) {
		if (customer.getId() == null || customer.getId().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		SimpleRedisDocumentRepository<CustomerDocument, ?> repo = cacheClient.getRepo(CustomerDocument.class);
		CustomerDocument updated = cacheClient.create(repo, customer);
		return ResponseEntity.ok(updated);
	}

	@PostMapping("/delete")
	public ResponseEntity<Void> delete(@RequestBody Map<String, String> request) {
		String id = request.get("id");
		if (!customerRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		customerRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}