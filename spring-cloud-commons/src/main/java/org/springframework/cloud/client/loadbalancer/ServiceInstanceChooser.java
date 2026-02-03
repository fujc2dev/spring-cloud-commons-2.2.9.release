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

import org.springframework.cloud.client.ServiceInstance;

/**
 * 它的职责非常单一和明确：根据一个服务名称（serviceId），从该服务的所有可用实例中选择一个。
 *
 * <p>
 *    这是一个设计分离，使用场景：用户有时候可能只需要获取一个服务实例信息
 * </p>
 *
 * @author Ryan Baxter
 */
public interface ServiceInstanceChooser {

    /**
     * 从负载均衡器中为指定服务选择一个服务实例。
     *
     * @param serviceId 服务ID
     * @return 服务实例
     */
    ServiceInstance choose(String serviceId);

}
