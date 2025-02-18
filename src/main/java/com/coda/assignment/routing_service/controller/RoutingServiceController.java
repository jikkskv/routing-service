package com.coda.assignment.routing_service.controller;

import com.coda.assignment.routing_service.loadbalancer.InstanceInfo;
import com.coda.assignment.routing_service.loadbalancer.ServiceRegistry;
import com.coda.assignment.routing_service.loadbalancer.strategies.LoadBalancer;
import com.coda.assignment.routing_service.service.JWTAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RestController
@Slf4j
public class RoutingServiceController {

    private final ServiceRegistry serviceRegistry;

    private final LoadBalancer loadBalancer;

    private final RestTemplate restTemplate;

    @Value("${jwt.validation:false}")
    public boolean validateJwtToken;

    @Autowired
    private JWTAuthorizationService jwtAuthorizationService;

    @Autowired
    RoutingServiceController(LoadBalancer loadBalancer, RestTemplate restTemplate, ServiceRegistry serviceRegistry) {
        this.loadBalancer = loadBalancer;
        this.restTemplate = restTemplate;
        this.serviceRegistry = serviceRegistry;
    }

    //    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    @PostMapping("/{serviceName}/**")
    public ResponseEntity<?> handlePostRequest(@RequestBody(required = false) String requestBody,
                                               @RequestHeader(required = false) MultiValueMap<String, String> headers,
                                               @PathVariable String serviceName,
                                               HttpServletRequest request) {
        Optional<InstanceInfo> optionalInstanceInfo = loadBalancer.getNextInstanceInfo(serviceName);
        if (optionalInstanceInfo.isEmpty()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        if (validateJwtToken && jwtAuthorizationService.validateJWTToken(request))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT Token");

        String forwardUrl = optionalInstanceInfo.get().getInstanceIPAddress() + request.getRequestURI().replace("/" + serviceName, "")  + "?" + request.getQueryString();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(forwardUrl, HttpMethod.valueOf(request.getMethod()), entity, Object.class);
    }

    @GetMapping("/register")
    public ResponseEntity<Boolean> register(@RequestParam(value = "serviceName", required = true) String serviceName,
                                            @RequestParam(value = "instanceIP", required = true) String instanceIP) {
        try {
            serviceRegistry.register(serviceName, instanceIP);
            return ResponseEntity.ok(true);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/deregister")
    public ResponseEntity<Boolean> deregister(@RequestParam(value = "serviceName", required = true) String serviceName,
                                          @RequestParam(value = "instanceIP", required = true) String instanceIP){
        try {
            serviceRegistry.deRegister(serviceName, instanceIP);
            return ResponseEntity.ok(true);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
