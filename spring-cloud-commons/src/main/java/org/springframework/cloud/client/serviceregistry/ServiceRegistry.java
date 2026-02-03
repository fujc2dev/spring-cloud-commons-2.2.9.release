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

package org.springframework.cloud.client.serviceregistry;

/**
 * 服务注册核心接口，定义了服务注册和注销的接口
 * <p>
 * 服务注册核心接口，定义了服务注册和注销的接口
 * </p>
 * <p>
 * {@link Registration } 是一个标记接口，代表一个服务实例的注册信息，不同的实现会有不同的具体实现类，比如：Eureka的EurekaRegistration。
 * </p>
 *
 * @param <R> registration meta data
 * @author Spencer Gibb
 * @since 1.2.0
 */
public interface ServiceRegistry<R extends Registration> {

    /**
     * 服务注册
     *
     * @param registration 元数据，默认 ip、port以及微服务名称，实现的话比如：Eureka的EurekaRegistration
     */
    void register(R registration);

    /**
     * 服务注销
     *
     * @param registration 元数据，默认 ip、port以及微服务名称，实现的话比如：Eureka的EurekaRegistration
     */
    void deregister(R registration);

    /**
     * 关闭服务，就目前我的理解就是不允许它被服务发现？
     */
    void close();

    /**
     * 设置注册状态。状态值由各个实现决定。
     *
     * @param registration 服务注册信息
     * @param status       状态数据
     * @see org.springframework.cloud.client.serviceregistry.endpoint.ServiceRegistryEndpoint
     */
    void setStatus(R registration, String status);

    /**
     * Gets the status of a particular registration.
     *
     * @param registration 服务注册信息
     * @param <T>          状态信息类型
     * @return 状态信息
     * @see org.springframework.cloud.client.serviceregistry.endpoint.ServiceRegistryEndpoint
     */
    <T> T getStatus(R registration);

}
