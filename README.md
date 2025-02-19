# routing-service

Test project for routing-service.

## Requirements
For building and running the application you need:

- [JDK 17](https://www.azul.com/downloads/?version=java-17-lts&os=linux&package=jdk#zulu)
- [Maven 3](https://maven.apache.org)
- [Spring Boot 3.4.2](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)


## Building a fat jar
```shell
cd ./routing-service
mvn clean package
```


## Testing the application

```shell
cd ./routing-service
mvn clean test
```
**86%** test coverage(70/81 lines)

## How to Run

```shell
cd ./routing-service
java -jar target/routing-service.jar
```


## Running the application locally

```shell
mvn spring-boot:run
```

## Implementation details
#### 
* **Initializer used:** [Spring Initializer](https://start.spring.io/)<br>
* **Data Structure:** [Atomic Integer](https://download.java.net/java/early_access/valhalla/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html) is used to achieve round-robin request distribution behaviour in multi-threading env.
* **APIs exposed:** handleRequest, register, deregister
#### Application Properties:

This section describes the configurable properties for the application. These properties can be set in the `application.properties` file or as environment variables.

#### General Configuration

| Property Name                   | Default Value                                  | Description                                                                                      |
|---------------------------------|------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `spring.application.name`       | `routing-service` | The name of the Spring Boot application.                                                         |
| `server.port`                   | `9191`                                           | The port on which the application runs.                                                          |
| `service.instance.address.json` | `{"serviceA":["http://10.238.31.2:8080", "http://10.238.56.43:8081", "http://10.238.45.78:8082"]}`                                         | JSON configuration containing service name and list of service instances.                        |
| `routing.strategy`              | `round_robin`                                         | The load-balancing strategy used to distribute requests.                                         |
| `jwt.validation`                             | `false`                                         | Enables or disables JWT token validation.                                                        |
| `jwt.secret` | `2CxClM+bOgS8rIRt36caFfyKFjLZW6FRTfG7PDx/ong=`                                         | The secret key used for JWT token signing and validation.                                        |
| `check.instance.heartbeat` | `false`                                         | Enables or disables instance heartbeat check. This will expiry instance if no heartbeat received. |


## How To test the routing service
* **Step 1 :** Run the routing-service (specified above).
* **Step 2 :** Run the simple-application-api (specified in README file of simple-application-api project).<br>
**NOTE:** <br>
  * Configure the 'routing-service.register.path' and 'routing-service.deregister.path' if 'routing-service' and 'simple-application-api' is running on different machines.<br>
  * We can run multiple instances of simple--application-api to see load request routing behaviour.
* **Step 3 :** Run the below curl request
```shell
curl -X POST "http://localhost:9191/simple-application-api/total_trips?start=2020-01-01&end=2020-01-02" \
     -H "Content-Type: application/json" \
     -d '{}'
```
Each time when above curl request is run, we can see from the response that it's calling different service instance.  
