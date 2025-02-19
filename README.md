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
#### Initializer used:
[Spring Initializer](https://start.spring.io/)
#### Application Properties:

This section describes the configurable properties for the application. These properties can be set in the `application.properties` file or as environment variables.

##### General Configuration

| Property Name                   | Default Value                                  | Description                                                                                      |
|---------------------------------|------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `spring.application.name`       | `routing-service` | The name of the Spring Boot application.                                                         |
| `server.port`                   | `9191`                                           | The port on which the application runs.                                                          |
| `service.instance.address.json` | `{"serviceA":["http://10.238.31.2:8080", "http://10.238.56.43:8081", "http://10.238.45.78:8082"]}`                                         | JSON configuration containing service name and list of service instances.                        |
| `routing.strategy`              | `round_robin`                                         | The load-balancing strategy used to distribute requests.                                         |
| `jwt.validation`                             | `false`                                         | Enables or disables JWT token validation.                                                        |
| `jwt.secret` | `2CxClM+bOgS8rIRt36caFfyKFjLZW6FRTfG7PDx/ong=`                                         | The secret key used for JWT token signing and validation.                                        |
| `check.instance.heartbeat` | `false`                                         | Enables or disables instance heartbeat check. This will expiry instance if no heartbeat received. |

