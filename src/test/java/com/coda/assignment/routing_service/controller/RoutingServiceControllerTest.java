package com.coda.assignment.routing_service.controller;

import com.coda.assignment.routing_service.loadbalancer.InstanceInfo;
import com.coda.assignment.routing_service.loadbalancer.ServiceRegistry;
import com.coda.assignment.routing_service.loadbalancer.strategies.LoadBalancer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingServiceControllerTest {

    @Mock
    private LoadBalancer loadBalancer;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServiceRegistry serviceRegistry;

    @InjectMocks
    private RoutingServiceController routingServiceController;

    private MultiValueMap<String, String> headers;

    private final String serviceName = "test-service";
    private final String instanceIP = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
    }

    @Test
    void testHandlePostRequest_SuccessfulForwarding() {
        InstanceInfo instanceInfo = new InstanceInfo("http://localhost:8081", 1000);
        when(loadBalancer.getNextInstanceInfo(anyString())).thenReturn(Optional.of(instanceInfo));
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getQueryString()).thenReturn("param=value");
        when(request.getMethod()).thenReturn("POST");

        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://localhost:8081/test?param=value"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Object.class))).thenReturn(mockResponse);

        ResponseEntity<?> response = routingServiceController.handlePostRequest("{}", headers, "serviceName", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate, times(1)).exchange(eq("http://localhost:8081/test?param=value"),
                eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testHandlePostRequest_NoAvailableInstance() {
        when(loadBalancer.getNextInstanceInfo(anyString())).thenReturn(Optional.empty());

        ResponseEntity<?> response = routingServiceController.handlePostRequest("{}", headers, "serviceName", request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testRegisterService_Success() {
        ResponseEntity<Boolean> response = routingServiceController.register(serviceName, instanceIP);
        verify(serviceRegistry, times(1)).register(serviceName, instanceIP);
        assertEquals(ResponseEntity.ok(true), response);
    }

    @Test
    void testRegisterService_Failure() {
        doThrow(new RuntimeException("Registration Failed")).when(serviceRegistry).register(serviceName, instanceIP);
        ResponseEntity<Boolean> response = routingServiceController.register(serviceName, instanceIP);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void testDeregisterService_Success() {
        ResponseEntity<Boolean> response = routingServiceController.deregister(serviceName, instanceIP);
        verify(serviceRegistry, times(1)).deRegister(serviceName, instanceIP);
        assertEquals(ResponseEntity.ok(true), response);
    }

    @Test
    void testDeregisterService_Failure() {
        doThrow(new RuntimeException("DeRegistration Failed")).when(serviceRegistry).deRegister(serviceName, instanceIP);
        ResponseEntity<Boolean> response = routingServiceController.deregister(serviceName, instanceIP);
        assertEquals(500, response.getStatusCode().value());
    }
}