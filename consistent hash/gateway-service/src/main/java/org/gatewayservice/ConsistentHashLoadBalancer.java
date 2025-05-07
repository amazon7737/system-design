package org.gatewayservice;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;


@Component
public class ConsistentHashLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    public ConsistentHashLoadBalancer(@Value("${spring.application.name}") String serviceId,
                                      ObjectProvider<ServiceInstanceListSupplier> provider) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = provider;
    }


    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        String hashKey = null;
        if (request.getContext() instanceof RequestDataContext ctx) {
            hashKey = ctx.getClientRequest().getHeaders().getFirst("X-Hash-Key");
        }
        if (hashKey == null) {
            hashKey = UUID.randomUUID().toString();
        }

        String finalHashKey = hashKey;
        return serviceInstanceListSupplierProvider.get().get(request)
                .map(instances -> getInstance(instances, finalHashKey));
    }

    private Response<ServiceInstance> getInstance(List<ServiceInstance> instances, String key) {
        if (instances.isEmpty()) {
            return new EmptyResponse();
        }
        int index = Math.abs(key.hashCode()) % instances.size();
        return new DefaultResponse(instances.get(index));
    }
}
