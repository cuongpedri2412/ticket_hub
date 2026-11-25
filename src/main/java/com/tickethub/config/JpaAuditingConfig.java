package com.tickethub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Bật tính năng tự điền @CreatedDate / @LastModifiedDate. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
