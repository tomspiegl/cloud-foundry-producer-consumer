# Cloud Foundry Eureka Service Registry Example

This example shows how to use the service discovery pattern using an Eureka services registry on Pivotal Cloud Foundry.

![Producer Service Discovery](producer-consumer-service-discovery.png)

**Service Registry:** The producer registers with the Service Registry. The Producer (client) provides metadata about itself, such as its host and port.
The Service Registry will call all application instances regularly to make sure the service is healthy (heartbeat).
 
**Producer:** The Producer application produces an incrementing counter (JSON Rest Controller). It registers itself with the Service Registry.

**Consumer:** Consumes the Rest API of the Producer. Uses the Service Registry to discover a running Producer endpoint.

Steps to create a service registry, configure container networking and how to build and push the producer and consumer applications:
1. [Create Service Registry Instance](#create-service-registry-instance)
1. [Producer Application](#producer-application)
1. [Consumer Application](#consumer-application)
1. [Container Networking](#container-networking)

Other Examples:
* [Spring Boot Eureka Demo](https://github.com/cloudfoundry/cf-networking-release/tree/develop/src/example-apps/eureka)

## Create Service Registry Instance

Switch to target org space.

`$ cf target -o myorg -s development`

Install Eureka Service Registry.

`$ cf marketplace -s p-service-registry`

Links: 
* [Pivotal Docu - Creating an Instance](https://docs.pivotal.io/spring-cloud-services/1-3/common/service-registry/creating-an-instance.html)
* [Pivotal Docu - Service Registry for Pivotal Cloud Foundry](https://docs.pivotal.io/spring-cloud-services/1-3/common/service-registry/)

## Producer Application

To build and deploy the producer on Cloud Foundry type:

```
$ cd producer
$ mvn clean package
$ cf push
```

### Producer Code 

**Producer.java**
```java
@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class Producer {

    private final AtomicInteger counter = new AtomicInteger();

    @RequestMapping(value = "/", produces = "application/json")
    public String counter() {
        return String.format("{\"producer-counter\":%d}", counter.incrementAndGet());
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Producer.class).web(true).run(args);
    }

}
```

Producer registers itself using `@EnableDiscoveryClient`.

**application.yml** 
```yaml
spring:
  application:
    name: box-consumer
  cloud:
    services:
      registrationMethod: direct

security:
  basic:
    enabled: false
```    

Disable the Rest Controller basic authentication (default in spring-boot-starter).

## Consumer Application

```
$ cd consumer
$ mvn clean package
$ cf push
```

**Consumer.java**
```java
@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class Consumer {

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RequestMapping(value = "/", produces = "application/json")
    public String consume() {
        Integer counter = (Integer) restTemplate.getForObject("http://box-producer/", Map.class).get("value");
        return String.format("{\"consumer-counter\":%d}", counter);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Consumer.class).web(true).run(args);
    }

}
```

Consumer registers itself using `@EnableDiscoveryClient`. 
It consumes the Producer service via `@LoadBalanced` RestTemplate.

## Container Networking  

`cf add-network-policy box-consumer --destination-app box-producer --protocol tcp --port 1-65535`

Current issue: `I/O error on GET request for "https://box-producer": Connection refused (Connection refused); nested exception is java.net.ConnectException: Connection refused (Connection refused)] with root cause`

Links
* [Pivotal Docu - Understanding C2C Networking](https://docs.cloudfoundry.org/concepts/understand-cf-networking.html) 
* [Pivotal- Docu - Understanding Application Security Groups](https://docs.pivotal.io/pivotalcf/1-11/concepts/asg.html)
* [CF - Networking on Github](https://github.com/cloudfoundry/cf-networking-release)
* [Container Networking with Cloud Foundry PWS/PCF - PART1](https://medium.com/@christophef/container-networking-with-cloudfoundry-pws-pcf-part1-8840d7f9a985)
