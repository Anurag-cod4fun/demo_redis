package com.example.demo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class DemoCacheJedisConnImplFactory {

	@Autowired Environment env;
	
	enum JedisCacheType {
		MASTER("MASTER"), RTWB("RTWB"), SERVICE("SERVICE");

		private String cacheTypeStr;
		
		JedisCacheType(String cacheTypeStr) {
			this.cacheTypeStr = cacheTypeStr;
		}
		
		public String getStr() {
			return cacheTypeStr;
		}
	}
	
	   /* ================= Helper Methods ================= */

    private String getNonEmptyProperty(String primary, String fallback) {
        String value = env.getProperty(primary);
        return (value != null && !value.isBlank()) ? value : env.getProperty(fallback);
    }
	
	
	@Primary
	@Bean({"rtwbJedisConnectionFactory", "redisConnectionFactory"})
	@ConditionalOnProperty(name = "SPRING.CACHE.RTWB.store", havingValue = "REDIS")
	public JedisConnectionFactory rtwb_JedisConnectionFactory() {
		return create_JedisConnectionFactory(JedisCacheType.RTWB);
	}
	
	private SSLSocketFactory getSslSocketFactory(String trustStorePath, String trustStorePassword,
			String CacheConfig_propPrefix) {
		try {
			String sslKeystoreInstance = env.getProperty(CacheConfig_propPrefix + "keystoreInstance", "JKS");
			String sslContextInstance = env.getProperty(CacheConfig_propPrefix + "sslContextInstance", "TLS");
			KeyStore trustStore = KeyStore.getInstance(sslKeystoreInstance);

			try (InputStream is = new FileInputStream(trustStorePath)) {
				trustStore.load(is, trustStorePassword.toCharArray());
			}
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);

			SSLContext sslContext = SSLContext.getInstance(sslContextInstance);
			sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

			return sslContext.getSocketFactory();
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException
				| KeyManagementException e) {
			return null;
		}
	}

	private JedisConnectionFactory create_JedisConnectionFactory(JedisCacheType cacheType) {

		final String CacheConfig_propPrefix = "SPRING.CACHE." + cacheType.getStr() + ".redis.";
		final String CacheJedisPoolConfig_propPrefix = "SPRING.CACHE." + cacheType.getStr() + ".redis.jedis.pool.";
		final boolean sslEnabled = "1".equals(env.getProperty(CacheConfig_propPrefix + "ssl.enable", "0"));

		
		// jedis conn config (use fallbacks and sensible defaults)
		String host = getNonEmptyProperty(CacheConfig_propPrefix + "host.DEFAULT", "spring.data.redis.host");
		String portStr = env.getProperty(CacheConfig_propPrefix + "port.DEFAULT", env.getProperty("spring.data.redis.port", "6379"));
		int port = 6379;
		try {
			port = Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
			// keep default
		}

		/*
		 * NOTE: check if host is not null and empty then take env specific password and client-name, else take default
		 */
		String password = getNonEmptyProperty(CacheConfig_propPrefix + "password.DEFAULT", "spring.data.redis.password");
		String clientName = env.getProperty(CacheConfig_propPrefix + "host");

		// jedis conn timeout config (ms)
		long connectTimeout = parseLongOrDefault(env.getProperty(CacheConfig_propPrefix + "connect-timeout.DEFAULT"), 2000L);
		long readTimeout = parseLongOrDefault(env.getProperty(CacheConfig_propPrefix + "read-timeout.DEFAULT"), 2000L);

		// jedis pool(jp) conn config
		int jpMaxTotalThreads = parseIntOrDefault(env.getProperty(CacheJedisPoolConfig_propPrefix + "max-total.DEFAULT"), 8);
		int jpMaxIdleThreads = parseIntOrDefault(env.getProperty(CacheJedisPoolConfig_propPrefix + "max-idle.DEFAULT"), 8);
		int jpMinIdleThreads = parseIntOrDefault(env.getProperty(CacheJedisPoolConfig_propPrefix + "min-idle.DEFAULT"), 0);
		boolean jpBlockWhenExhausted = "1".equals(env.getProperty(CacheJedisPoolConfig_propPrefix + "block-when-exhausted", "0"));
		int jpMaxWaitMillis = parseIntOrDefault(env.getProperty(CacheJedisPoolConfig_propPrefix + "max-wait-millis.DEFAULT"), 1000);
		int jpMinEvictableIdleTimeMillis = parseIntOrDefault(env.getProperty(CacheJedisPoolConfig_propPrefix + "min-evictable-idle-time-millis.DEFAULT"), 60000);
		int jpTimeBetweenEvictionRunsMillis = parseIntOrDefault(env.getProperty(CacheJedisPoolConfig_propPrefix + "time-between-eviction-runs-millis.DEFAULT"), 30000);
		boolean jpTestOnBorrow = "1".equals(env.getProperty(CacheJedisPoolConfig_propPrefix + "test-on-borrow.DEFAULT", "0"));
		boolean jpTestWhileIdle = "1".equals(env.getProperty(CacheJedisPoolConfig_propPrefix + "test-while-idle.DEFAULT", "0"));
		boolean jpTestOnReturn = "1".equals(env.getProperty(CacheJedisPoolConfig_propPrefix + "test-on-return.DEFAULT", "0"));
		int jpNumTestsPerEvictionRun = parseIntOrDefault(env.getProperty(CacheJedisPoolConfig_propPrefix + "num-tests-per-eviction-run.DEFAULT"), 3);
		boolean isEarlyStartUp = "1".equals(env.getProperty(CacheConfig_propPrefix + "earlyStartup", "0"));
		
		final RedisStandaloneConfiguration redisStandaloneConfig = new RedisStandaloneConfiguration(host, port);
		if (password != null && !password.isBlank()) {
			redisStandaloneConfig.setPassword(password);
		}
		
		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(jpMaxTotalThreads);
		jedisPoolConfig.setMaxIdle(jpMaxIdleThreads);
		jedisPoolConfig.setMinIdle(jpMinIdleThreads);
		jedisPoolConfig.setBlockWhenExhausted(jpBlockWhenExhausted);
		jedisPoolConfig.setMaxWait(Duration.ofMillis(jpMaxWaitMillis));
		jedisPoolConfig.setMinEvictableIdleDuration(Duration.ofMillis(jpMinEvictableIdleTimeMillis));
		jedisPoolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(jpTimeBetweenEvictionRunsMillis));
		jedisPoolConfig.setTestOnBorrow(jpTestOnBorrow);
		jedisPoolConfig.setTestWhileIdle(jpTestWhileIdle);
		jedisPoolConfig.setTestOnReturn(jpTestOnReturn);
		jedisPoolConfig.setNumTestsPerEvictionRun(jpNumTestsPerEvictionRun);
		
		
		JedisClientConfigurationBuilder jedisCCBldr = JedisClientConfiguration.builder();
		JedisClientConfiguration jedisCC = null;
		
		if (clientName != null) jedisCCBldr = jedisCCBldr.clientName(clientName);

		if(sslEnabled) {
			String trustStorePath = env.getProperty(CacheConfig_propPrefix + "ssl.truststore.path");
			String trustStorePassword = env.getProperty(CacheConfig_propPrefix + "ssl.truststore.password");

			SSLSocketFactory sslSocketFactory = getSslSocketFactory(trustStorePath,trustStorePassword,CacheConfig_propPrefix);
			jedisCC = jedisCCBldr.connectTimeout(Duration.ofMillis(connectTimeout))
					.readTimeout(Duration.ofMillis(readTimeout))
					.usePooling().poolConfig(jedisPoolConfig)
					.and().useSsl().sslSocketFactory(sslSocketFactory)
					.build();
			
		} else {
			
			jedisCC = jedisCCBldr.connectTimeout(Duration.ofMillis(connectTimeout))
					.readTimeout(Duration.ofMillis(readTimeout))
					.usePooling().poolConfig(jedisPoolConfig)
					// .and().useSsl().hostnameVerifier(null).sslParameters(null).sslSocketFactory(null)
					.build();
		}
		
		JedisConnectionFactory jedisConnFactory = new JedisConnectionFactory(redisStandaloneConfig, jedisCC);
		jedisConnFactory.setEarlyStartup(isEarlyStartUp);
		jedisConnFactory.afterPropertiesSet(); // required to initialize jedis client config
		
		return jedisConnFactory;
	}

	private int parseIntOrDefault(String s, int def) {
		if (s == null || s.isBlank()) return def;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private long parseLongOrDefault(String s, long def) {
		if (s == null || s.isBlank()) return def;
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}
}
