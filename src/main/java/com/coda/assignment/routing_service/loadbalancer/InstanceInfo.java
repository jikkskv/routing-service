package com.coda.assignment.routing_service.loadbalancer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(exclude = "lastActiveTime")
@AllArgsConstructor
@Data
public class InstanceInfo {
    private String instanceIPAddress;
    private long lastActiveTime;
}
