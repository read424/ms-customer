package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class RootController {

    @GetMapping("/")
    public Mono<Void> redirectToSwagger(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        response.getHeaders().setLocation(java.net.URI.create("/swagger-ui.html"));
        return response.setComplete();
    }
}
