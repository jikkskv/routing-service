package com.coda.assignment.routing_service.loadbalancer.strategies;

import com.coda.assignment.routing_service.loadbalancer.InstanceInfo;
import com.coda.assignment.routing_service.loadbalancer.ServiceRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoundRobinLoadBalancerTest {

    @Mock
    private ServiceRegistry serviceRegistry;

    @InjectMocks
    private RoundRobinLoadBalancer loadBalancer;

    private final List<InstanceInfo> instanceList = List.of(
            new InstanceInfo("192.168.1.1", 8080),
            new InstanceInfo("192.168.1.2", 8081),
            new InstanceInfo("192.168.1.3", 8082)
    );

    @Test
    void testGetNextInstanceInfo_roundRobinSelection() {
        when(serviceRegistry.getInstanceList("test-service")).thenReturn(instanceList);
        assertEquals("192.168.1.1", loadBalancer.getNextInstanceInfo("test-service").get().getInstanceIPAddress());
        assertEquals("192.168.1.2", loadBalancer.getNextInstanceInfo("test-service").get().getInstanceIPAddress());
        assertEquals("192.168.1.3", loadBalancer.getNextInstanceInfo("test-service").get().getInstanceIPAddress());
        assertEquals("192.168.1.1", loadBalancer.getNextInstanceInfo("test-service").get().getInstanceIPAddress());
    }

    @Test
    void testGetNextInstanceInfo_emptyInstanceList() {
        when(serviceRegistry.getInstanceList("empty-service")).thenReturn(List.of());
        assertTrue(loadBalancer.getNextInstanceInfo("empty-service").isEmpty());
    }

    @Test
    void testGetNextInstanceInfo_multithreadingHighLoad() throws InterruptedException {
        when(serviceRegistry.getInstanceList("test-service")).thenReturn(instanceList);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        int totalRequest = 1_000_000, allowedMarginalError = 1_000;

        for (int idx = 0; idx < totalRequest; idx++) {
            executorService.execute(() -> {
                Optional<InstanceInfo> instance = loadBalancer.getNextInstanceInfo("test-service");
                assertTrue(instance.isPresent());
                String ipAddress = instance.get().getInstanceIPAddress();
                results.add(ipAddress);
            });
        }
        executorService.shutdown();
        boolean gracefulTerminated = executorService.awaitTermination(2, TimeUnit.MINUTES);
        Map<String, Long> instanceCountMap = results.stream().collect(Collectors.groupingBy(i -> i, Collectors.counting()));

        assertTrue(gracefulTerminated, "Executor Service could not gracefully terminate before allowed timeout period");
        for (String instanceIP : instanceList.stream().map(InstanceInfo::getInstanceIPAddress).toList()) {
            assertTrue(instanceCountMap.containsKey(instanceIP));
            assertTrue(Math.abs(instanceCountMap.get(instanceIP) - (totalRequest / instanceList.size())) <= allowedMarginalError, "Instance count not properly evened");
        }
    }

    @Test
    void testRoundRobin_withDynamicInstances() throws InterruptedException {
        List<InstanceInfo> cloneInstanceList = new ArrayList<>(instanceList);
        when(serviceRegistry.getInstanceList("test-service")).thenReturn(cloneInstanceList);
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int idx = 0; idx < 10; idx++) {
            executorService.execute(() -> {
                Optional<InstanceInfo> instance = loadBalancer.getNextInstanceInfo("test-service");
                assertTrue(instance.isPresent());
                String ipAddress = instance.get().getInstanceIPAddress();
                results.add(ipAddress);
            });

            // Simulate instance removal
            if (idx == 5) {
                cloneInstanceList.remove(0); // Remove the first instance dynamically
            }
        }
        executorService.shutdown();
        boolean gracefulTerminated = executorService.awaitTermination(1, TimeUnit.MINUTES);

        assertTrue(gracefulTerminated, "Executor Service could not gracefully terminate before allowed timeout period");
        assertFalse(results.isEmpty(), "There should be at least some successful responses.");
        assertTrue(results.containsAll(List.of("192.168.1.2", "192.168.1.3")), "Only remaining instances should be used.");
    }

    @Test
    void testGetNextInstanceInfo_withMultipleServices() throws InterruptedException {
        when(serviceRegistry.getInstanceList("test-serviceA")).thenReturn(instanceList);
        List<InstanceInfo> serviceBInstanceList = List.of(
                new InstanceInfo("192.235.1.1", 8080),
                new InstanceInfo("192.235.1.2", 8081));
        when(serviceRegistry.getInstanceList("test-serviceB")).thenReturn(serviceBInstanceList);

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<String> resultA = Collections.synchronizedList(new ArrayList<>());
        List<String> resultB = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 1_000; i++) {
            executorService.execute(() -> {
                Optional<InstanceInfo> instance = loadBalancer.getNextInstanceInfo("serviceA");
                assertTrue(instance.isPresent());
                String ipAddress = instance.get().getInstanceIPAddress();
                resultA.add(ipAddress);
            });

            executorService.execute(() -> {
                Optional<InstanceInfo> instance = loadBalancer.getNextInstanceInfo("serviceB");
                assertTrue(instance.isPresent());
                String ipAddress = instance.get().getInstanceIPAddress();
                resultB.add(ipAddress);
            });
        }
        executorService.shutdown();
        boolean gracefulTerminated = executorService.awaitTermination(1, TimeUnit.MINUTES);
        assertTrue(gracefulTerminated, "Executor Service could not gracefully terminate before allowed timeout period");
        assertFalse(resultA.stream().anyMatch(resultB::contains));
        assertFalse(resultB.stream().anyMatch(resultA::contains));
    }
}