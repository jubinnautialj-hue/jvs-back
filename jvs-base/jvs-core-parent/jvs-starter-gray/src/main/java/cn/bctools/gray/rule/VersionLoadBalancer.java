package cn.bctools.gray.rule;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.cloud.nacos.balancer.NacosBalancer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessage;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * jvs微服务负载规则介绍
 * 默认负载排序
 * 版本号(是否开启强制版本号)>权重>轮训)
 * 强制版本号:版本a服务,版本b服务,  那么请求c服务时, 直接返回失败
 * 非强制版本:版本a服务,版本b服务,  那么请求c服务时, 判断其它版本的(权重>轮训)
 *
 * <p>
 * 实现了以下几个几种方式：
 * 1. 版本号  : 根据不同的版本号进行区分,版本号做是否强制操作
 * 2. 权重（Weighted）：每个服务器可以设置权重，根据权重的比例分配请求。
 * 3. 轮询（Round Robin）：每个请求按顺序分配到不同的服务器上，如果服务器A后面是服务器B，那么下一个请求就会分配给服务器B。
 * <p>
 * 常见还有以下两种,根据真实情况看不否实现
 * 4.随机（Random）：每个请求随机分配到各个服务器上。
 * 5.最少连接（Least Connections）：优先分配给当前连接数最少的服务器。
 * 6. 源地址哈希（Source IP Hash）：根据访问源IP的哈希结果分配，保证同一IP的访问会分配到同一服务器。
 * 这些规则可以根据实际业务需求和服务器性能进行选择和调整。
 *
 * @author Administrator
 */
@Slf4j
public class VersionLoadBalancer extends RoundRobinLoadBalancer {

    private static final String IPV4_REGEX = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";

    private static final String IPV6_KEY = "IPv6";

    private final String serviceId;

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    public VersionLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        super(serviceInstanceListSupplierProvider, serviceId);
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(serviceInstances -> getInstanceResponse(serviceInstances, request));
    }

    /**
     * 服务路由规则
     *
     * @param instances
     * @param request
     * @return
     */
    @SneakyThrows
    Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {

        // 服务注册中心无可用实例, 抛出异常
        if (CollUtil.isEmpty(instances)) {
            log.warn("No instance available. serviceId: {}", serviceId);
            return new EmptyResponse();
        }
        if (instances.size() == 1) {
            return new DefaultResponse(instances.get(0));
        }

        // 获取版本号, 获取异常时直接返回权重
        if (request == null || request.getContext() == null) {
            return new DefaultResponse(NacosBalancer
                    .getHostByRandomWeight3(instances));
        }

        DefaultRequestContext requestContext = (DefaultRequestContext) request.getContext();
        if (!(requestContext.getClientRequest() instanceof RequestData)) {
            return new DefaultResponse(NacosBalancer
                    .getHostByRandomWeight3(instances));
        }

        RequestData clientRequest = (RequestData) requestContext.getClientRequest();
        HttpHeaders headers = clientRequest.getHeaders();

        String reqVersion = headers.getFirst(SysConstant.VERSION);
        if (StrUtil.isNotBlank(reqVersion)) {
            reqVersion = URLDecoder.decode(reqVersion, Charset.defaultCharset().name());
        } else {
            log.info("No version found");
            return new DefaultResponse(NacosBalancer
                    .getHostByRandomWeight3(instances));
        }

        // 筛选元数据匹配的实例
        String finalReqVersion = reqVersion;
        List<ServiceInstance> matchedInstances = instances.stream().filter(instance -> {
            NacosServiceInstance nacosInstance = (NacosServiceInstance) instance;
            Map<String, String> metadata = nacosInstance.getMetadata();
            String targetVersion = metadata.getOrDefault(SysConstant.VERSION, "-");
            return targetVersion.contains(finalReqVersion);
        }).collect(Collectors.toList());

        if (CollUtil.isNotEmpty(matchedInstances)) {
            // 如果版本一样,则在这中间根据权重返回数据
            log.info("Found {} matched instance, returns a random one. serviceId: {}, version: {}", matchedInstances.size(), serviceId, reqVersion);
            ServiceInstance instance = NacosBalancer
                    .getHostByRandomWeight3(matchedInstances);
            return new DefaultResponse(instance);
        } else {
            // 不存在, 返回任意一个实例
            log.warn("No instance matched. serviceId: {}, version: {}", serviceId, reqVersion);
            ServiceInstance instance = NacosBalancer
                    .getHostByRandomWeight3(instances);
            return new DefaultResponse(instance);
        }
    }


    /**
     * 根据serviceId筛选可用服务
     *
     * @param discoveryClient 服务中心对象
     * @param serviceId       服务ID
     * @param request         当前请求
     * @return 服务实例
     */
    public ServiceInstance choose(DiscoveryClient discoveryClient, String serviceId, HttpMessage request) {
        // 获取版本号
        String version = request.getHeaders().getFirst(SysConstant.VERSION);
        return choose(discoveryClient, serviceId, version);
    }


    /**
     * 根据serviceId筛选可用服务
     *
     * @param discoveryClient 服务中心对象
     * @param serviceId       服务id
     * @param version         版本号(路由标识)
     * @return 服务实例
     */
    @SneakyThrows
    public ServiceInstance choose(DiscoveryClient discoveryClient, String serviceId, String version) {
        if (StringUtils.isNotBlank(version)) {
            version = URLDecoder.decode(version, Charset.defaultCharset().name());
        }
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        // 注册中心无实例, 抛出异常
        if (ObjectUtils.isEmpty(instances)) {
            log.warn("No instance available for {}", serviceId);
            throw new BusinessException("No instance available for " + serviceId);
        }
        // 同网段随机找一个实例出来
        // 获取当前启动的集群中的这个实例
        return defaultVersionOrRandomInstance(discoveryClient, instances, version);
    }


    /**
     * 获取默认服务实例
     * <p>
     * 1、找是否有与网关相同的版本号,随机一个
     * 2、找是否有默认标识版本号
     * 3、随机找一个版本号
     *
     * @param instances 服务实例集合
     * @param version   版本号(路由标识)
     * @return 服务实例
     */
    private ServiceInstance defaultVersionOrRandomInstance(DiscoveryClient discoveryClient, List<ServiceInstance> instances, String version) {
        List<ServiceInstance> matchedInstances = instances.stream()
                .filter(instance -> MapUtil.getStr(instance.getMetadata(), SysConstant.VERSION, "-").contains(version))
                .collect(Collectors.toList());
        if (ObjectUtils.isNotEmpty(matchedInstances)) {
            return NacosBalancer.getHostByRandomWeight3(matchedInstances);
        }
        //如果没有找到版本号，默认使用网关相同的版本号。
        ServiceInstance serviceInstance = discoveryClient.getInstances(SpringContextUtil.getApplicationContextName())
                .stream()
                .findFirst().get();
        //如果有版本号优先找和网关相同的版本
        String gatewayVersion = serviceInstance.getMetadata().getOrDefault(SysConstant.VERSION, SpringContextUtil.getVersion());
        List<ServiceInstance> serviceInstances = instances.stream()
                .filter(instance -> MapUtil.getStr(instance.getMetadata(), SysConstant.VERSION, "-").contains(gatewayVersion))
                .collect(Collectors.toList());
        if (ObjectUtils.isNotEmpty(serviceInstances)) {
            return NacosBalancer.getHostByRandomWeight3(serviceInstances);
        }
        // 找有没有默认版本号
        Optional<ServiceInstance> first = instances.stream().filter(e -> e.getMetadata().containsValue(SysConstant.DEFAULT)).findFirst();
        // 随机一个版本号
        return first.orElseGet(() -> instances.get(RandomUtil.randomInt(instances.size())));
    }

}
