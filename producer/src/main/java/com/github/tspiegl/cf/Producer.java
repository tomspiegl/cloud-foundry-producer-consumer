package com.github.tspiegl.cf;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class Producer {

    private final AtomicInteger counter = new AtomicInteger();

    @RequestMapping(value = "/", produces = "application/json")
    public String counter() {
        return String.format("{\"counter\"=%d}", counter.incrementAndGet());
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Producer.class).web(true).run(args);
    }

}