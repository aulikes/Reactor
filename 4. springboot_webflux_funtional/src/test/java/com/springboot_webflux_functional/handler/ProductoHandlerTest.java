package com.springboot_webflux_functional.handler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan("com.springboot_webflux_functional.config")
class ProductoHandlerTest {

    @Test
    void pruebaTest() {
        System.out.println("🚀 Prueba UNITARIA ejecutada.");
    }
}