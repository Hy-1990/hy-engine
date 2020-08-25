package com.huyi.web;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/** @author huyi */
@EnableSwaggerBootstrapUI
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan("com.huyi")
@EnableMethodCache(basePackages = "com.huyi")
@EnableCreateCacheAnnotation
@EnableScheduling
public class WebApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebApplication.class, args);
  }
}
