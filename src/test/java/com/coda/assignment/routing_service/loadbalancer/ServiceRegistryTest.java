package com.coda.assignment.routing_service.loadbalancer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceRegistryTest {

    @Test
    public void testRegister_singleService() {
        ServiceRegistry serviceRegistry = new ServiceRegistry("{}", false);
        serviceRegistry.register("service1", "instanceA");
        serviceRegistry.register("service1", "instanceB");
        serviceRegistry.register("service1", "instanceC");
        serviceRegistry.register("service1", "instanceD");

        List<InstanceInfo> instanceInfo = serviceRegistry.getInstanceList("service1");
        assertNotNull(instanceInfo);
        assertFalse(instanceInfo.isEmpty());
        assertEquals(4, instanceInfo.size());
    }

    @Test
    public void testRegister_testExpiry() throws InterruptedException {
        ServiceRegistry serviceRegistry = new ServiceRegistry("{}", true);
        serviceRegistry.register("service1", "instanceA");
        serviceRegistry.register("service1", "instanceB");
        Thread.sleep(10_000 + 1_000);
        serviceRegistry.register("service1", "instanceC");
        serviceRegistry.register("service1", "instanceD");

        List<InstanceInfo> instanceInfo = serviceRegistry.getInstanceList("service1");
        assertNotNull(instanceInfo);
        assertFalse(instanceInfo.isEmpty());
        assertEquals(2, instanceInfo.size());
    }

    @Test
    public void testRegister_invalidServiceDeRegister() {
        ServiceRegistry serviceRegistry = new ServiceRegistry("{}", false);
        serviceRegistry.register("service1", "instanceA");
        serviceRegistry.deRegister("service1", "instanceC");

        List<InstanceInfo> instanceInfo = serviceRegistry.getInstanceList("service1");
        assertNotNull(instanceInfo);
        assertFalse(instanceInfo.isEmpty());
        assertEquals(1, instanceInfo.size());
    }

    @Test
    public void testRegister_invalidInstanceDeRegister() {
        ServiceRegistry serviceRegistry = new ServiceRegistry("{}", false);
        serviceRegistry.deRegister("service1", "instanceC");

        List<InstanceInfo> instanceInfo1 = serviceRegistry.getInstanceList("service1");
        assertNotNull(instanceInfo1);
        assertTrue(instanceInfo1.isEmpty());
    }

    @Test
    public void testRegister_singleServiceDeRegister() {
        ServiceRegistry serviceRegistry = new ServiceRegistry("{}", false);
        serviceRegistry.register("service1", "instanceA");
        serviceRegistry.register("service1", "instanceB");
        serviceRegistry.register("service2", "instanceC");
        serviceRegistry.register("service3", "instanceD");
        serviceRegistry.deRegister("service2", "instanceC");

        List<InstanceInfo> instanceInfo1 = serviceRegistry.getInstanceList("service1");
        List<InstanceInfo> instanceInfo2 = serviceRegistry.getInstanceList("service2");
        List<InstanceInfo> instanceInfo3 = serviceRegistry.getInstanceList("service3");
        assertNotNull(instanceInfo1);
        assertFalse(instanceInfo1.isEmpty());
        assertEquals(2, instanceInfo1.size());
        assertNotNull(instanceInfo2);
        assertTrue(instanceInfo2.isEmpty());
        assertNotNull(instanceInfo3);
        assertFalse(instanceInfo3.isEmpty());
        assertEquals(1, instanceInfo3.size());
    }
}