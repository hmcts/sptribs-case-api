package uk.gov.hmcts.sptribs.bankholidays.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfiguration {

        @Bean
        public CacheManager bankHolidayCacheManager() {
            CaffeineCacheManager manager = new CaffeineCacheManager("scottish_bank_holiday_cache");
            manager.setCaffeine(Caffeine.newBuilder()
                    .expireAfterWrite(24, TimeUnit.HOURS)
                    .maximumSize(1));
            return manager;
        }
}
