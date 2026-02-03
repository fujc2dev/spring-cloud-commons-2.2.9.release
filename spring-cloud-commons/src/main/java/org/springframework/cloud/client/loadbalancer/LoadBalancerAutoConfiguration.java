/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.client.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * SpringCloud中负载均衡自动装配配置。
 * <pre>
 * 1. 该类收集所有被 @{@link LoadBalanced} 注解修饰的 {@link RestTemplate} Bean，并添加到 restTemplates 列表中。
 * 2. 允许开发者定义自己{@link LoadBalancerRequestTransformer}，在执行过程中干预{@link HttpRequest}、{@link ServiceInstance}两个对象。
 * 3. {@link SmartInitializingSingleton}, 在容器启动完成后，会调用其afterSingletonsInstantiated方法，对restTemplates进行增强。
 * 4. {@link LoadBalancerRequestFactory}, 用于创建LoadBalancerRequest，并添加LoadBalancerRequestTransformer，
 *    它主要的用处将开发者定义的干预的逻辑，添加到LoadBalancerRequest中，标准的Spring的尿性。
 * 5. 情况1：用于未使用spring-retry框架的场景下，拦截器{@link LoadBalancerInterceptor}，生效。
 *    情况2：用于使用了spring-retry框架的场景下，拦截器{@link RetryLoadBalancerInterceptor}，生效。
 *    问：这个拦截器存在的意义是什么？
 *    答：在微服务调用中，我们用的是大多情况都是微服务名称+path。需要有个地方将微服务名称+path转为实际的ip地址，
 *    并在此过程中实现了客户端的负载均衡，再将请求转发给LoadBalancerClient，
 *    它们之间的唯一区别的，一个有重试逻辑一个是一次性执行。
 * </pre>
 *
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Will Tran
 * @author Gang Li
 */
// 表示它是一个配置类
@Configuration(proxyBeanMethods = false)
// 因为负载均衡的调用依赖这个RestTemplate，必须要依赖这个包（即引入了spring-web）
@ConditionalOnClass(RestTemplate.class)
// 服务要有自己的LoadBalancerClient实现
@ConditionalOnBean(LoadBalancerClient.class)
// 负载均衡重试机制核心配置
@EnableConfigurationProperties(LoadBalancerRetryProperties.class)
public class LoadBalancerAutoConfiguration {

    /**
     * 负载均衡的RestTemplate定制器
     * <p>
     * 它的作用是告诉Spring：“请将所有被 @LoadBalanced 注解修饰的 RestTemplate Bean收集到这个列表中”。
     * </p>
     * <pre>
     * @Bean
     * @LoadBalanced // 这个RestTemplate会被收集到上面的restTemplates列表中
     * public RestTemplate restTemplate() {
     *     return new RestTemplate();
     * }
     * </pre>
     */
    @LoadBalanced
    @Autowired(required = false)
    private List<RestTemplate> restTemplates = Collections.emptyList();

    /**
     * 向请求处理流程中添加自定义逻辑
     * <pre>
     *     场景1：传递链路追踪信息（如 TraceId）
     *     场景2：基于实例元数据（Metadata）的透传
     *     场景3：动态路由或灰度发布
     * </ppre>
     */
    @Autowired(required = false)
    private List<LoadBalancerRequestTransformer> transformers = Collections.emptyList();

    /**
     * 这是延迟Bean，在容器启动完成后，会调用其afterSingletonsInstantiated方法，对restTemplates进行增强。
     *
     * @param restTemplateCustomizers 用户自定义的RestTemplate定制器
     * @return {@link SmartInitializingSingleton}
     */
    @Bean
    public SmartInitializingSingleton loadBalancedRestTemplateInitializerDeprecated(final ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
        return () -> restTemplateCustomizers.ifAvailable(customizers -> {
            for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
                for (RestTemplateCustomizer customizer : customizers) {
                    customizer.customize(restTemplate);
                }
            }
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancerRequestFactory loadBalancerRequestFactory(LoadBalancerClient loadBalancerClient) {
        return new LoadBalancerRequestFactory(loadBalancerClient, this.transformers);
    }

    @Configuration(proxyBeanMethods = false)
    // 无spring-retry生效
    @ConditionalOnMissingClass("org.springframework.retry.support.RetryTemplate")
    static class LoadBalancerInterceptorConfig {

        @Bean
        public LoadBalancerInterceptor loadBalancerInterceptor(LoadBalancerClient loadBalancerClient, LoadBalancerRequestFactory requestFactory) {
            return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public RestTemplateCustomizer restTemplateCustomizer(final LoadBalancerInterceptor loadBalancerInterceptor) {
            return restTemplate -> {
                List<ClientHttpRequestInterceptor> list = new ArrayList<>(restTemplate.getInterceptors());
                list.add(loadBalancerInterceptor);
                restTemplate.setInterceptors(list);
            };
        }

    }

    /**
     * Auto configuration for retry mechanism.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RetryTemplate.class) // spring-retry
    public static class RetryAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public LoadBalancedRetryFactory loadBalancedRetryFactory() {
            return new LoadBalancedRetryFactory() {
            };
        }

    }

    /**
     * Auto configuration for retry intercepting mechanism.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RetryTemplate.class) // spring-retry
    public static class RetryInterceptorAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public RetryLoadBalancerInterceptor loadBalancerRetryInterceptor(LoadBalancerClient loadBalancerClient, LoadBalancerRetryProperties properties, LoadBalancerRequestFactory requestFactory, LoadBalancedRetryFactory loadBalancedRetryFactory) {
            return new RetryLoadBalancerInterceptor(loadBalancerClient, properties, requestFactory, loadBalancedRetryFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public RestTemplateCustomizer restTemplateCustomizer(final RetryLoadBalancerInterceptor loadBalancerInterceptor) {
            return restTemplate -> {
                List<ClientHttpRequestInterceptor> list = new ArrayList<>(restTemplate.getInterceptors());
                list.add(loadBalancerInterceptor);
                restTemplate.setInterceptors(list);
            };
        }

    }

}
