package com.huyi.web.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huyi.common.utils.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** @Author huyi @Date 2020/7/21 19:25 @Description: redis 配置 */
@Configuration
public class RedisConfig {

  private RedisTemplate redisTemplate;
  /**
   * 配置第一个数据源的RedisTemplate 并且标识第一个数据源是默认数据源 @Primary
   *
   * @return
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    Jackson2JsonRedisSerializer jackson2JsonRedisSerializer =
        new Jackson2JsonRedisSerializer(Object.class);
    ObjectMapper om = new ObjectMapper();
    om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    jackson2JsonRedisSerializer.setObjectMapper(om);
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    // key采用String的序列化方式
    template.setKeySerializer(stringRedisSerializer);
    // hash的key也采用String的序列化方式
    template.setHashKeySerializer(stringRedisSerializer);
    // value序列化方式采用jackson
    template.setValueSerializer(jackson2JsonRedisSerializer);
    // hash的value序列化方式采用jackson
    template.setHashValueSerializer(jackson2JsonRedisSerializer);
    template.afterPropertiesSet();
    return template;
  }

  /**
   * 设置数据存入 redis 的序列化方式,并开启事务
   *
   * @param redisTemplate
   * @param factory
   */
  private void initDomainRedisTemplate(
      RedisTemplate<String, Object> redisTemplate, RedisConnectionFactory factory) {
    // 如果不配置Serializer，那么存储的时候缺省使用String，如果用User类型存储，那么会提示错误User can't cast to String！
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    // 开启事务
    //        redisTemplate.setEnableTransactionSupport(true); //先注释掉(先关闭redis事务，提交后立刻生效)
    redisTemplate.setConnectionFactory(factory);
  }

  /**
   * 注入封装RedisTemplate @Title: redisUtil
   *
   * @return RedisUtil
   * @date 2018年6月21日
   * @throws
   */
  @Bean(name = "redisUtil")
  public RedisUtil redisUtil(RedisTemplate<String, Object> redisTemplate) {
    RedisUtil redisUtil = new RedisUtil();
    redisUtil.setRedisTemplate(redisTemplate);
    return redisUtil;
  }
}
