package com.springboot_webflux_functional.config;

import com.springboot_webflux_functional.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler){

        return route(
                GET("/api/v2/productos").or(GET("/api/v3/productos")), handler::listar)
            .andRoute(GET("/api/v2/productos/{id}"), handler::ver)
            .andRoute(POST("/api/v2/productos"), handler::crear)
            .andRoute(PUT("/api/v2/productos/{id}"), handler::editarComplete)
            .andRoute(PATCH("/api/v2/productos/{id}"), handler::editarPartial)
            .andRoute(DELETE("/api/v2/productos/{id}"), handler::eliminar)
            .andRoute(POST("/api/v2/productos/upload/{id}"), handler::upload)
            .andRoute(POST("/api/v2/productos/crear"), handler::crearConFoto);
    }

}
