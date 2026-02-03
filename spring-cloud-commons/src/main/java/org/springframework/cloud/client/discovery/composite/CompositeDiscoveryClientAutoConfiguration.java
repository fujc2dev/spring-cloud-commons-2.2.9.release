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

package org.springframework.cloud.client.discovery.composite;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Auto-configuration for composite discovery client.
 *
 * @author Biju Kunjummen
 */

@Configuration(proxyBeanMethods = false)
// CompositeDiscoveryClientAutoConfiguration在SimpleDiscoveryClientAutoConfiguration前被加载
// 也就是优先配置有Nacos等外部注册的配置，
// 如果没有外部注册的配置，则使用SimpleDiscoveryClientAutoConfiguration
@AutoConfigureBefore(SimpleDiscoveryClientAutoConfiguration.class)
public class CompositeDiscoveryClientAutoConfiguration {

    @Bean
    // SpringCloud 允许第三方厂家实现 DiscoveryClient 接口，但是调用得由我（SpringCloud）来
    // SpringCloud 自己提供了一个聚合的CompositeDiscoveryClient
    @Primary
    public CompositeDiscoveryClient compositeDiscoveryClient(List<DiscoveryClient> discoveryClients) {
        return new CompositeDiscoveryClient(discoveryClients);
    }

}
