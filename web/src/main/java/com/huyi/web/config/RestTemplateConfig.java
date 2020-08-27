package com.huyi.web.config;

import com.huyi.common.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/** @Author huyi @Date 2020/7/17 12:37 @Description */
@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate httpsRestTemplate(HttpComponentsClientHttpRequestFactory httpsFactory) {
    RestTemplate restTemplate = new RestTemplate(httpsFactory);
    restTemplate.setErrorHandler(
        new ResponseErrorHandler() {
          @Override
          public boolean hasError(ClientHttpResponse clientHttpResponse) {
            return false;
          }

          @Override
          public void handleError(ClientHttpResponse clientHttpResponse) {
            // 默认处理非200的返回，会抛异常
          }
        });
    return restTemplate;
  }

  @Bean(name = "httpsFactory")
  public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory()
      throws Exception {
    CloseableHttpClient httpClient = HttpClientUtils.acceptsUntrustedCertsHttpClient();
    HttpComponentsClientHttpRequestFactory httpsFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);
    httpsFactory.setReadTimeout(2000);
    httpsFactory.setConnectTimeout(2000);
    return httpsFactory;
  }
}
