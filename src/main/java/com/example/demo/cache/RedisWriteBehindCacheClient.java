package com.example.demo.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.config.RTWBRedisModelRepositories;
import com.redis.om.spring.repository.support.SimpleRedisDocumentRepository;

@Component
public class RedisWriteBehindCacheClient {

    @Autowired
    private RTWBRedisModelRepositories rtwbRedisModelRepositories;

    public <E> SimpleRedisDocumentRepository<E, ?> getRepo(Class<E> domainClass) {
        if (domainClass == null) throw new IllegalArgumentException("Entity class must not be null");
        try {
            return rtwbRedisModelRepositories.getDocumentRepository(domainClass);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while fetching repository for entity: " + domainClass, e);
        }
    }

    public <E> E create(SimpleRedisDocumentRepository<E, ?> repo, E entityBean) {
        if (repo == null) throw new IllegalArgumentException("Repo object must not be null");
        try {
            return repo.save(entityBean);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in create method", e);
        }
    }

}
