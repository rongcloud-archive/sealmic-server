package cn.rongcloud;

import cn.rongcloud.common.JwtTokenHelper;
import cn.rongcloud.config.JwtProperties;
import cn.rongcloud.filter.GlobalExceptionHandlerAdvice;
import cn.rongcloud.filter.JwtFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@Slf4j
@Configuration
public class SealMicConfiguration {
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private  JwtFilter jwtFilter;

    @Bean
    public <K, V> RedisTemplate<K, V> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("config redisTemplate");
        RedisTemplate<K, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public GlobalExceptionHandlerAdvice globalExceptionAdvice() {
        log.info("config globalExceptionAdvice");
        return new GlobalExceptionHandlerAdvice();
    }

    @Bean
    public JwtTokenHelper jwtTokenHelper() {
        log.info("config jwtTokenHelper expired {} ms", jwtProperties.getTtlInMilliSec());
        return jwtProperties.getTtlInMilliSec() == null
                ? new JwtTokenHelper(jwtProperties.getSecret())
                : new JwtTokenHelper(jwtProperties.getSecret(), jwtProperties.getTtlInMilliSec());
    }

    @Bean
    public FilterRegistrationBean jwtTokenFilter() {
        log.info("config jwtTokenFilter");
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public WebMvcConfigurer getWebMvcConfigurerAdapter() {
        return new WebMvcConfigurer() {
            @Override
            public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
                Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
                while (iterator.hasNext()) {
                    HttpMessageConverter<?> c = iterator.next();
                    if (c instanceof MappingJackson2HttpMessageConverter) {
                        iterator.remove();
                    }
                }
                FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
                List<MediaType> supportedMediaTypes = new ArrayList<>();
                supportedMediaTypes.add(MediaType.APPLICATION_JSON);
                supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
                supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
                supportedMediaTypes.add(MediaType.MULTIPART_FORM_DATA);
                converter.setSupportedMediaTypes(supportedMediaTypes);
                FastJsonConfig fastJsonConfig = new FastJsonConfig();
                fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue, SerializerFeature.PrettyFormat);
                converter.setFastJsonConfig(fastJsonConfig);
                converters.add(converter);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "cn.rongcloud.web", value = "enableCors", havingValue = "true")
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
