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

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import reactor.util.retry.RetryBackoffSpec;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

/**
 * 该类是一个{@link LoadBalancerClient}配置属性类（Configuration Properties），
 * 它的作用是将应用程序配置文件（如 application.yml）中以 spring.cloud.loadbalancer.retry
 * 为前缀的属性映射到 Java 对象的字段上。
 *
 * @author Ryan Baxter
 */
@ConfigurationProperties("spring.cloud.loadbalancer.retry")
public class LoadBalancerRetryProperties {

    /**
     * 总开关。设置为 false 将完全禁用 LoadBalancer 层面的重试功能
     */
    private boolean enabled = true;

    /**
     * 默认只对 GET 请求进行重试（因为 GET 是幂等的）。
     * <p>
     * {@link HttpMethod#GET}.
     */
    private boolean retryOnAllOperations = false;

    /**
     * 在同一实例上的重试次数。
     * <p>
     * 当第一次请求到一个实例失败后，框架会立即在同一个实例上重试最多 maxRetriesOnSameServiceInstance 次。
     * </p>
     * <p>
     * 适用场景: 解决暂时的网络抖动或服务实例短暂的GC停顿。如果重试成功，可以避免切换到另一个实例的开销。
     * </p>
     */
    private int maxRetriesOnSameServiceInstance = 0;

    /**
     * 在后续不同实例上的重试次数。
     * <p>
     * 当在同一个实例上的重试（如果配置了）都失败后，LoadBalancer 会重新选择一个新实例，然后在新实例上进行请求。
     * 这个动作最多重复 maxRetriesOnNextServiceInstance 次
     * </p>
     * <p>
     * 计算总重试次数: 总重试次数 = 1 (初始请求) + maxRetriesOnSameServiceInstance + maxRetriesOnNextServiceInstanc
     * </p>
     */
    private int maxRetriesOnNextServiceInstance = 1;

    /**
     * 可以指定某些HTTP状态码也触发重试。
     * <p>
     * 举例: 如果配置 retryableStatusCodes: [500, 502]，
     * 那么当服务端返回 500（内部错误）或 502（网关错误）时，LoadBalancer 也会尝试重试。
     * </p>
     */
    private Set<Integer> retryableStatusCodes = new HashSet<>();

    /**
     * 重试退避策略 (Backoff 内部类)
     * <p>
     * 为了防止在服务短暂不可用时，大量客户端同时重试导致“重试风暴”，该类提供了基于 Reactor 的退避策略。
     * </p>
     */
    private Backoff backoff = new Backoff();

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRetryOnAllOperations() {
        return retryOnAllOperations;
    }

    public void setRetryOnAllOperations(boolean retryOnAllOperations) {
        this.retryOnAllOperations = retryOnAllOperations;
    }

    public int getMaxRetriesOnSameServiceInstance() {
        return maxRetriesOnSameServiceInstance;
    }

    public void setMaxRetriesOnSameServiceInstance(int maxRetriesOnSameServiceInstance) {
        this.maxRetriesOnSameServiceInstance = maxRetriesOnSameServiceInstance;
    }

    public int getMaxRetriesOnNextServiceInstance() {
        return maxRetriesOnNextServiceInstance;
    }

    public void setMaxRetriesOnNextServiceInstance(int maxRetriesOnNextServiceInstance) {
        this.maxRetriesOnNextServiceInstance = maxRetriesOnNextServiceInstance;
    }

    public Set<Integer> getRetryableStatusCodes() {
        return retryableStatusCodes;
    }

    public void setRetryableStatusCodes(Set<Integer> retryableStatusCodes) {
        this.retryableStatusCodes = retryableStatusCodes;
    }

    public Backoff getBackoff() {
        return backoff;
    }

    public void setBackoff(Backoff backoff) {
        this.backoff = backoff;
    }

    public static class Backoff {

        /**
         * backoff.enabled (默认: false): 退避策略的总开关。
         */
        private boolean enabled = false;

        /**
         * minBackoff (默认: 5ms): 第一次重试的最小等待时间。
         * <p>
         * Used to set {@link RetryBackoffSpec#minBackoff}.
         * </p>
         */
        private Duration minBackoff = Duration.ofMillis(5);

        /**
         * maxBackoff (默认: Long.MAX_VALUE): 重试等待时间的上限。
         * <p>
         * Used to set {@link RetryBackoffSpec#maxBackoff}.
         * </p>
         */
        private Duration maxBackoff = Duration.ofMillis(Long.MAX_VALUE);

        /**
         * 随机因子（抖动）。每次重试的等待时间会在计算值的基础上增加一个随机扰动，避免多个客户端同步重试。
         * <p>
         * Used to set {@link RetryBackoffSpec#jitter}.
         * </p>
         */
        private double jitter = 0.5d;

        public Duration getMinBackoff() {
            return minBackoff;
        }

        public void setMinBackoff(Duration minBackoff) {
            this.minBackoff = minBackoff;
        }

        public Duration getMaxBackoff() {
            return maxBackoff;
        }

        public void setMaxBackoff(Duration maxBackoff) {
            this.maxBackoff = maxBackoff;
        }

        public double getJitter() {
            return jitter;
        }

        public void setJitter(double jitter) {
            this.jitter = jitter;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

}
