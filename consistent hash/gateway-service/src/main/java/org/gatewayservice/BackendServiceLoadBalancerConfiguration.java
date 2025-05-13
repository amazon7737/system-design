package org.gatewayservice;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackendServiceLoadBalancerConfiguration {

    @Bean
    public ReactorServiceInstanceLoadBalancer serviceInstanceLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider
    ){
        return new ConsistentHashLoadBalancer(serviceInstanceListSupplierProvider);
    }

}
