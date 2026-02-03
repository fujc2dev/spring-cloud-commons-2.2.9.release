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

package org.springframework.cloud.client.discovery;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.Ordered;

/**
 * 这个接口定义了服务发现的抽象。
 * <p>
 * 策略模式。负载均衡器（如 OpenFeign 或 LoadBalancerClient）就是通过调用这个接口来获取服务列表，从而实现负载均衡的。
 * </p>
 * <p>
 * 任何服务注册中心的客户端想要融入 Spring Cloud 生态，都必须实现这个接口
 * </p>
 *
 * @author Spencer Gibb
 * @author Olga Maciaszek-Sharma
 * @author Chris Bono
 */
public interface DiscoveryClient extends Ordered {

    /**
     * Default order of the discovery client.
     */
    int DEFAULT_ORDER = 0;

    /**
     * HealthIndicator 中使用的可读实现描述。
     *
     * @return 返回该实现的描述信息，主要用于调试、日志和 Actuator 的 HealthIndicator。
     */
    String description();

    /**
     * 这是最常用的方法，根据服务名（如 user-service）获取所有健康实例的列表。
     *
     * @param serviceId 微服务名称
     * @return {@link List<ServiceInstance> }
     */
    List<ServiceInstance> getInstances(String serviceId);

    /**
     * 获取当前注册中心中所有已注册的服务名称列表
     *
     * @return ["user-service", "order-service", "gateway"].
     */
    List<String> getServices();

    /**
     * 一个轻量级健康检查方法，用于验证此 DiscoveryClient 实例本身是否健康，能否与后端的注册中心正常通信
     */
    default void probe() {
        getServices();
    }

    /**
     * 实现 Ordered 接口，定义该 DiscoveryClient 实现的加载顺序。
     *
     * @return DEFAULT_ORDER（值为 0）。
     */
    @Override
    default int getOrder() {
        return DEFAULT_ORDER;
    }

}
