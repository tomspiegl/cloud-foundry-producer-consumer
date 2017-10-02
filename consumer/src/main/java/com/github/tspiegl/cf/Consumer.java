package com.github.tspiegl.cf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
        Integer counter = (Integer) restTemplate.getForObject("//box-producer", Map.class).get("value");
        return String.format("{\"consumer-counter\":%d}", counter);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Consumer.class).web(true).run(args);
    }

}