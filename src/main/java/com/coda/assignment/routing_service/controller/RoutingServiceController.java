package com.coda.assignment.routing_service.controller;

import com.coda.assignment.routing_service.loadbalancer.InstanceInfo;
import com.coda.assignment.routing_service.loadbalancer.strategies.LoadBalancer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RestController
public class RoutingServiceController {

    private final LoadBalancer loadBalancer;

    private final RestTemplate restTemplate;

    @Autowired
    RoutingServiceController(LoadBalancer loadBalancer, RestTemplate restTemplate) {
        this.loadBalancer = loadBalancer;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/**")
    public ResponseEntity<?> handlePostRequest(@RequestBody(required = false) String requestBody, @RequestHeader(required = false) MultiValueMap<String, String> headers,
                                               HttpServletRequest request) {
        Optional<InstanceInfo> optionalInstanceInfo = loadBalancer.getNextInstanceInfo(request.getServerName());
        if (optionalInstanceInfo.isEmpty()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        String forwardUrl = optionalInstanceInfo.get().getInstanceIPAddress() + request.getRequestURI() + "?" + request.getQueryString();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(forwardUrl, HttpMethod.valueOf(request.getMethod()), entity, Object.class);
    }

}
