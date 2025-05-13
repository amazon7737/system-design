package org.gatewayservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
public class ConsistentHashLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final ConsistentHash<ServiceInstance> consistentHash;
    private final Logger log = LoggerFactory.getLogger("ConsistentHashLoadBalancer");

    public ConsistentHashLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> provider) {
        this.serviceInstanceListSupplierProvider = provider;
        this.serviceId = "backend-service";
        this.consistentHash = new ConsistentHash<>();
    }


    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable();
        if (supplier == null) {
            log.warn("No ServiceInstanceListSupplier available");
            return Mono.just(new EmptyResponse());
        }

        return supplier.get().next().map(serviceInstances -> {
            if (serviceInstances.isEmpty()) {
                log.warn("No service instances available");
                return new EmptyResponse();
            }

            String userId = extractUserId(request);
            consistentHash.update(serviceInstances);

            ServiceInstance selected = consistentHash.get(userId).orElseThrow(() -> new RuntimeException("로드밸런싱에 문제가 있습니다."));

            if (selected == null) {
                log.warn("No instance selected for userId={}", userId);
                return new EmptyResponse();
            }

            log.info("Selected instance for userId={}: {}:{}", userId, selected.getHost(), selected.getPort());
            return new DefaultResponse(selected);
        });
    }


    private String extractUserId(Request request) {
        if (request instanceof RequestDataContext rdc) {
            RequestData data = rdc.getClientRequest();
            URI url = data.getUrl();
            String query = url.getQuery(); // 예: userId=alice&other=1

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("userId")) {
                        return keyValue[1];
                    }
                }
            }
        }
        return "default";
    }
}
