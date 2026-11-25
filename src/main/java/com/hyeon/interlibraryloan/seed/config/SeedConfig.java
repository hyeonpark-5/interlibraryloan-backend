package com.hyeon.interlibraryloan.seed.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SeedProperties.class)
public class SeedConfig {
}
