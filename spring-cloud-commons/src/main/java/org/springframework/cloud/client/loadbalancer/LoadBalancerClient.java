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

import java.io.IOException;
import java.net.URI;

import org.springframework.cloud.client.ServiceInstance;

/**
 * SpringCloud 定义了LoadBalancerClient接口。
 * <p>
 * 这个接口是连接“服务发现”和“HTTP客户端”的桥梁。
 * </p>
 *
 * @author Spencer Gibb
 */
public interface LoadBalancerClient extends ServiceInstanceChooser {

    /**
     * 核心方法：使用服务名（serviceId）来执行请求，替代原有的主机和端口
     *
     * @param serviceId 服务名
     * @param request   请求
     * @param <T>       泛型
     * @return 响应
     * @throws IOException
     */
    <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException;

    /**
     * 根据服务名，解析出一个具体的服务实例（ServiceInstance）
     *
     * @param serviceId       服务名
     * @param serviceInstance 服务实例
     * @param request         请求
     * @param <T>             泛型
     * @return 响应
     * @throws IOException
     */
    <T> T execute(String serviceId, ServiceInstance serviceInstance,
                  LoadBalancerRequest<T> request) throws IOException;

    /**
     * 将类似 http://myservice/path 的URI重构为 http://real-host:port/path
     *
     * @param instance 服务实例
     * @param original 原始URI
     * @return 构建后的URI
     */
    URI reconstructURI(ServiceInstance instance, URI original);

}
