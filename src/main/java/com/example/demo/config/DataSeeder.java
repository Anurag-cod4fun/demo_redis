package com.example.demo.config;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.demo.cache.RedisWriteBehindCacheClient;
import com.example.demo.document.CustomerDocument;
import com.redis.om.spring.repository.support.SimpleRedisDocumentRepository;

@Component
@Profile("test")
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RedisWriteBehindCacheClient cacheClient;

    @Override
    public void run(String... args) throws Exception {
        try {
            CustomerDocument c = new CustomerDocument(UUID.randomUUID().toString(), "Alice", "alice@example.com", "ACTIVE", "seeded");
            SimpleRedisDocumentRepository<CustomerDocument, ?> repo = cacheClient.getRepo(CustomerDocument.class);
            CustomerDocument saved = cacheClient.create(repo, c);
            System.out.println("Seeded Customer id=" + saved.getId());
        } catch (Throwable t) {
            System.err.println("DataSeeder failed: " + t.getMessage());
        }
    }
}
