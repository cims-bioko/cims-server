package com.github.cimsbioko.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;

@Configuration
@EnableCaching
public class CachingConfig {

    public static final String USER_CACHE = "userCache";
    public static final String CAMPAIGN_MEMBERSHIP_CACHE = "campaignMembershipCache";
    public static final String MY_CAMPAIGNS_CACHE = "campaignMembershipCache";

    @Bean
    public CacheManager cacheManager(@Value("${app.caffeine.spec:expireAfterAccess=5m}") String cacheSpec) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheSpecification(cacheSpec);
        return manager;
    }

    @Bean
    public UserCache userCache(CacheManager cacheManager) throws Exception {
        return new SpringCacheBasedUserCache(cacheManager.getCache(USER_CACHE));
    }
}
