package com.coda.assignment.routing_service.loadbalancer.strategies;

import com.coda.assignment.routing_service.loadbalancer.InstanceInfo;
import com.coda.assignment.routing_service.loadbalancer.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(name = "routing.strategy", havingValue = "round_robin")
public class RoundRobinLoadBalancer extends LoadBalancer {

    private final Map<String, AtomicInteger> instancePointerMap;

    protected RoundRobinLoadBalancer(@Autowired ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
        instancePointerMap = new HashMap<>();
    }

    @Override
    public Optional<InstanceInfo> getNextInstanceInfo(String serviceName) {
        List<InstanceInfo> instanceList = serviceRegistry.getInstanceList(serviceName);
        if (instanceList.isEmpty()) {
            return Optional.empty();
        }
        AtomicInteger instancePointer = instancePointerMap.computeIfAbsent(serviceName, e -> new AtomicInteger(-1));
        instancePointer.compareAndSet(Integer.MAX_VALUE, -1);
        return Optional.of(instanceList.get(instancePointer.incrementAndGet() % instanceList.size()));
    }
}
