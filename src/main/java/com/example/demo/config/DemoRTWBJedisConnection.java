package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.RedisModulesOperations;

@Configuration
@ConditionalOnProperty(name = "SPRING.CACHE.RTWB.store", havingValue = "REDIS")
public class DemoRTWBJedisConnection {

	@Bean("redisTemplate") 
	RedisTemplate<String, String> redisTemplate(@Qualifier("rtwbJedisConnectionFactory") JedisConnectionFactory rtwbJedisConnectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(rtwbJedisConnectionFactory);
		template.setKeySerializer(new GenericToStringSerializer<Object>(Object.class));
		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

		return template;
	}
	
	@Bean("rtwbStringRedisTemplate")
	public StringRedisTemplate stringRedisTemplate(@Qualifier("rtwbJedisConnectionFactory") JedisConnectionFactory redisConnectionFactory) {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
		stringRedisTemplate.setKeySerializer(RedisSerializer.string());
		stringRedisTemplate.setValueSerializer(RedisSerializer.string());
		return stringRedisTemplate;
	}

	@Primary
	@Bean(name = "redisModulesOperations") 
	RedisModulesOperations<?> redisModulesOperations( 
			@Qualifier("rtwbRedisModulesClient") RedisModulesClient rmc, 
			@Qualifier("rtwbStringRedisTemplate") StringRedisTemplate template, 
			@Qualifier("omGsonBuilder") GsonBuilder gsonBuilder) {
		return new RedisModulesOperations<>(rmc, template, gsonBuilder);
	}
	
	@Bean(name = "rtwbRedisModulesClient")
	RedisModulesClient rtwbRedisModulesClient(
			@Qualifier("rtwbJedisConnectionFactory") JedisConnectionFactory rtwbJedisConnectionFactory,
			@Qualifier("omGsonBuilder") GsonBuilder builder) {
		return new RedisModulesClient(rtwbJedisConnectionFactory, builder);
	}
	
}