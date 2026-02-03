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

package org.springframework.cloud.client.discovery.simple;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * 这个类的存在是为了满足一个特定的需求：
 * 当你的微服务架构中不想（或不能）部署一个完整的、中心化的服务注册中心
 * （如 Nacos、Eureka）时，如何依然能使用 Spring Cloud 的服务发现和负载均衡机制？
 * <p>
 * 1.创建一个简单的服务注册中心，如 SimpleDiscoveryProperties
 * <pre>
 * # application.yml 示例
 * spring:
 *   cloud:
 *     discovery:
 *       client:
 *         simple:
 *           instances:
 *             user-service:
 *               - uri: http://localhost:8081
 *                 metadata:
 *                   zone: zone1
 *               - uri: http://localhost:8082
 *                 metadata:
 *                   zone: zone2
 *             order-service:
 *               - uri: http://localhost:9091
 *     </pre>
 * 2. 例子： 假设5个服务分别是：gateway-service, user-service, order-service, product-service, auth-service，怎么基于{@link SimpleDiscoveryClient}构建呢？
 * <pre>
 * # 关闭注册中心
 * spring:
 *   cloud:
 *     # 禁用服务注册（因为我们只用发现，不向中心注册）
 *     service-registry:
 *       auto-registration:
 *         enabled: false
 * 2.1 网关服务 (gateway-service) 的配置
 * # gateway-service的 application.yml
 * server:
 *   port: 8080
 *
 * spring:
 *   application:
 *     name: gateway-service # 当前服务的名称
 *   cloud:
 *     discovery:
 *       client:
 *         simple:
 *           instances:
 *             user-service:
 *               - uri: http://localhost:8081
 *               # 可以添加元数据，如版本、权重等
 *               # - metadata:
 *               #     version: v1
 *             order-service:
 *               - uri: http://localhost:8082
 *             product-service:
 *               - uri: http://localhost:8083
 *             auth-service:
 *               - uri: http://localhost:8084
 *     # 如果使用了Spring Cloud Gateway，负载均衡默认已开启
 *     gateway:
 *       discovery:
 *         locator:
 *           enabled: true # 允许通过服务名进行路由
 * 2.2  用户服务 (user-service) 的配置
 * # user-service的 application.yml
 * server:
 *   port: 8081
 *
 * spring:
 *   application:
 *     name: user-service
 *   cloud:
 *     discovery:
 *       client:
 *         simple:
 *           instances:
 *             gateway-service:
 *               - uri: http://localhost:8080
 *             order-service:
 *               - uri: http://localhost:8082
 *             product-service:
 *               - uri: http://localhost:8083
 *             auth-service:
 *               - uri: http://localhost:8084
 * </pre>
 * </p>
 */
public class SimpleDiscoveryClient implements DiscoveryClient {

    private SimpleDiscoveryProperties simpleDiscoveryProperties;

    public SimpleDiscoveryClient(SimpleDiscoveryProperties simpleDiscoveryProperties) {
        this.simpleDiscoveryProperties = simpleDiscoveryProperties;
    }

    @Override
    public String description() {
        return "Simple Discovery Client";
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        List<ServiceInstance> serviceInstances = new ArrayList<>();
        List<DefaultServiceInstance> serviceInstanceForService = this.simpleDiscoveryProperties
                .getInstances().get(serviceId);

        if (serviceInstanceForService != null) {
            serviceInstances.addAll(serviceInstanceForService);
        }
        return serviceInstances;
    }

    @Override
    public List<String> getServices() {
        return new ArrayList<>(this.simpleDiscoveryProperties.getInstances().keySet());
    }

    @Override
    public int getOrder() {
        return this.simpleDiscoveryProperties.getOrder();
    }

}
