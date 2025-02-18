package com.coda.assignment.routing_service.loadbalancer.strategies;

import com.coda.assignment.routing_service.loadbalancer.InstanceInfo;
import com.coda.assignment.routing_service.loadbalancer.ServiceRegistry;

import java.util.Optional;

public abstract class LoadBalancer {

    protected ServiceRegistry serviceRegistry;

    protected LoadBalancer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public abstract Optional<InstanceInfo> getNextInstanceInfo(String serviceName);
}
