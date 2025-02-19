package com.coda.assignment.routing_service.loadbalancer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@Slf4j
public class ServiceRegistry {

    private static final long SLEEP_TIME = 10_000;
    private final ConcurrentMap<String, Set<InstanceInfo>> registry;
    private static final ObjectMapper objectMapper = new ObjectMapper();



    @SuppressWarnings("unchecked")
    public ServiceRegistry(@Value("${service.instance.address.json:{}}") String serviceInstanceAddressJson,
                           @Value("${check.instance.heartbeat:false}") boolean checkForHeartBeat) {
        registry = new ConcurrentHashMap<>();
        try {
            Map<String, List<String>> serviceInstanceMap = objectMapper.readValue(serviceInstanceAddressJson, Map.class);
            serviceInstanceMap.forEach((key, value) -> value.forEach(instance -> this.register(key, instance)));
            scheduleExpirationTask(checkForHeartBeat);
        } catch (JsonProcessingException e) {
            log.error("Parsing error occurred in parsing value of service.instance.address.json : {}", serviceInstanceAddressJson, e);
        }
    }

    private void scheduleExpirationTask(boolean checkForHeartBeat) {
        if (checkForHeartBeat) {
            new Thread(() -> {
                while (true) {
                    try {
                        registry.forEach((key, value) -> value.removeIf(instanceInfo -> (System.currentTimeMillis() - instanceInfo.getLastActiveTime()) > SLEEP_TIME));
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    public void register(String serviceName, String instanceIP) {
        log.info("Registering started for serviceName: {}, instanceIP: {} to the load balancer: ", serviceName, instanceIP);
        registry.computeIfAbsent(serviceName, e -> new CopyOnWriteArraySet<>()).add(new InstanceInfo(instanceIP, System.currentTimeMillis()));
        log.info("Registering completed for serviceName: {}, instanceIP: {} to the load balancer: ", serviceName, instanceIP);
    }

    public void deRegister(String serviceName, String instanceIP) {
        if (Objects.nonNull(registry.get(serviceName)) && !registry.get(serviceName).isEmpty()) {
            log.info("DeRegistering started for serviceName: {}, instanceIP: {} to the load balancer: ", serviceName, instanceIP);
            Optional<InstanceInfo> optionalInstanceInfo = registry.get(serviceName).stream().filter(e -> Objects.equals(e.getInstanceIPAddress(), instanceIP)).findAny();
            optionalInstanceInfo.ifPresent(e -> registry.get(serviceName).remove(optionalInstanceInfo.get()));
            log.info("DeRegistering completed for serviceName: {}, instanceIP: {} to the load balancer: ", serviceName, instanceIP);
        }
    }

    public List<InstanceInfo> getInstanceList(String serviceName) {
        return registry.getOrDefault(serviceName, Collections.emptySet()).stream().toList();
    }
}
