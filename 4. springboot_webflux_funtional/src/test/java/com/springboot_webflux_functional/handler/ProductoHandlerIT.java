package com.springboot_webflux_functional.handler;

import com.springboot_webflux_functional.models.documents.Producto;
import com.springboot_webflux_functional.models.services.CategoriaService;
import com.springboot_webflux_functional.models.services.ProductoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")  // 🔥 Activa la configuración específica de pruebas E2E
@ComponentScan("com.springboot_webflux_functional.config")
class ProductoHandlerIT {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Value("${config.base.endpoint}")
    private String url;

    @Test
    void listarTest() {
        client.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
            .expectBodyList(Producto.class)
            .consumeWith(response -> {
                List<Producto> productos = response.getResponseBody();
                productos.forEach(p -> {
                    System.out.println(p.getNombre());
                });
                Assertions.assertThat(!productos.isEmpty()).isTrue();
            });
    }

    @Test
    void verTest() {
        Producto producto = productoService.findByNombre("TV Panasonic Pantalla LCD").block();
        client.get()
            .uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
            /*.expectBody() //para probar solo son el path de JSON
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD");*/
            //PARA UNA MANERA MÁS DETALLADA
            .expectBody(Producto.class)
            .consumeWith(response -> {
                Producto p = response.getResponseBody();
                Assertions.assertThat(p.getId()).isNotEmpty();
                Assertions.assertThat(p.getNombre()).isEqualTo("TV Panasonic Pantalla LCD");
            });
    }
}