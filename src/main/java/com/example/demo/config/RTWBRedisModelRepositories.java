package com.example.demo.config;

import com.redis.om.spring.repository.support.SimpleRedisDocumentRepository;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.repository.core.MappingRedisEntityInformation;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.CustomRedisKeyValueTemplate;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.support.SimpleRedisDocumentRepository;
import com.redis.om.spring.vectorize.Embedder;

@Component
@ConditionalOnProperty(name = "SPRING.CACHE.RTWB.store", havingValue = "REDIS")
public class RTWBRedisModelRepositories {

	@Autowired
	@Qualifier("redisJSONKeyValueTemplate")
	private CustomRedisKeyValueTemplate keyValueTemplate;
	
	@Autowired
	@Qualifier("redisModulesOperations")
	private RedisModulesOperations<?> rmo;
	
	@Autowired private RediSearchIndexer keyspaceToIndexMap;
	@Autowired private RedisMappingContext mappingContext;
	@Autowired private GsonBuilder gsonBuilder;
	@Autowired private RedisOMProperties redisOMProperties;
	@Autowired private Embedder embedder;
	
	private Map<Class<?>, SimpleRedisDocumentRepository<?,?>> repoMapping = new ConcurrentHashMap<>();
	
	public <T> SimpleRedisDocumentRepository<T, Object> getDocumentRepository(Class<T> entityClass) {
		
		SimpleRedisDocumentRepository<T, ?> repo = null;
		
		if (!repoMapping.containsKey(entityClass)) {
			EntityInformation<T, ?> info = new MappingRedisEntityInformation(
					keyValueTemplate.getMappingContext().getPersistentEntity(entityClass));
			
			if(info != null) {
				repo = new SimpleRedisDocumentRepository(info, keyValueTemplate, rmo, keyspaceToIndexMap, mappingContext,
					gsonBuilder, embedder, redisOMProperties);
			}
			repoMapping.put(entityClass, repo);
		}else {
			repo = (SimpleRedisDocumentRepository<T, ?>) repoMapping.get(entityClass);
		}
		
		if (repo == null) {
			System.out.println("repo cannot be null");
		}
		
		return (SimpleRedisDocumentRepository<T, Object>) repo;
	}
	
	public String getKey(Class<?> entityClass, Object id) {
		return keyspaceToIndexMap.getKeyspaceForEntityClass(entityClass) + id;
	}
}
