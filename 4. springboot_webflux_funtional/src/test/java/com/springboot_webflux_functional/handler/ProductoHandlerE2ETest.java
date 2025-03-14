package com.springboot_webflux_functional.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot_webflux_functional.models.documents.Categoria;
import com.springboot_webflux_functional.models.documents.Producto;
import com.springboot_webflux_functional.models.services.CategoriaService;
import com.springboot_webflux_functional.models.services.ProductoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")  // 🔥 Activa la configuración específica de pruebas E2E
@ComponentScan("com.springboot_webflux_functional.config")
class ProductoHandlerE2ETest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Value("${config.base.endpoint}")
    private String url;

    private List<Producto> backupProductos;
    private List<Categoria> backupCategorias;

    @BeforeEach
    void backupDatabase() {
        // Guardamos el estado original de la base de datos
        backupProductos = productoService.findAllProducto().collectList().block();
        backupCategorias = categoriaService.findAllCategoria().collectList().block();
    }

    @AfterEach
    void restoreDatabase() {
        // Primero eliminamos TODOS los productos
        productoService.deleteAll()
                .then(categoriaService.deleteAll())  // Luego eliminamos TODAS las categorías
                .thenMany(Flux.fromIterable(backupCategorias))
                .flatMap(categoriaService::saveCategoria) // Restauramos primero las categorías
                .thenMany(Flux.fromIterable(backupProductos))
                .flatMap(productoService::save)  // Restauramos los productos
                .blockLast();
    }

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

    @Test
    void crearTest() { //VERIFICANDO DE FORMA JSON
        Categoria categoria = categoriaService.findByNombre("Muebles").block();
        Producto producto = new Producto("Mesa comedor", 100.00, categoria);
        client.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(producto), Producto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
            .expectBody()
            .jsonPath("$.producto.id").isNotEmpty()
            .jsonPath("$.producto.nombre").isEqualTo("Mesa comedor")
            .jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
    }

    @Test
    public void crear2Test() { //VERIFICANDO CON EL OBJETO PRODUCTO
        Categoria categoria = categoriaService.findByNombre("Muebles").block();
        Producto producto = new Producto("Mesa comedor2", 100.00, categoria);
        client.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(producto), Producto.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
            .expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
            .consumeWith(response -> {
                Object o = response.getResponseBody().get("producto");
                Producto p = new ObjectMapper().convertValue(o, Producto.class);
                Assertions.assertThat(p.getId()).isNotEmpty();
                Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor2");
                Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
            });
    }

    @Test
    void editarCompleteTest() {
        Producto producto = productoService.findByNombre("Sony Notebook").block();
        Categoria categoria = categoriaService.findByNombre("Electrónico").block();

        Producto productoEditado = new Producto("Asus Notebook", 700.00, categoria);

        client.put().uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(productoEditado), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Asus Notebook")
                .jsonPath("$.categoria.nombre").isEqualTo("Electrónico");
    }

    @Test
    void eliminarTest() {
        Producto producto = productoService.findByNombre("Mica Cómoda 5 Cajones").block();
        client.delete()
            .uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
            .exchange()
            .expectStatus().isNoContent()
            .expectBody()
            .isEmpty();

        client.get()
            .uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .isEmpty();
    }
}