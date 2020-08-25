package com.huyi.web.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger2 配置类
 *
 * @author vi
 * @since 2019/3/6 8:31 PM
 */
@EnableSwagger2
@Configuration
public class Swagger2Config implements WebMvcConfigurer {
  @Autowired private ProjectConfig projectConfig;

  @Bean
  public Docket createRestApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo())
        .select()
        .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
        .paths(PathSelectors.any())
        .build();
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("hy-engine")
        .description("Restful-API-Doc")
        .termsOfServiceUrl(projectConfig.getUrl())
        .contact(
            new Contact(
                projectConfig.getName(), projectConfig.getCompanyName(), projectConfig.getEmail()))
        .version(projectConfig.getVersion())
        .build();
  }
}
