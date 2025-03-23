package com.springboot_webflux_apirest.controllers;

import com.springboot_webflux_apirest.exceptions.ProductoNotFoundException;
import com.springboot_webflux_apirest.models.documents.Producto;
import com.springboot_webflux_apirest.models.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Value("${config.uploads.path}")
    private String path;

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> lista(){
        return Mono.just(
            ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(productoService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> ver(@PathVariable String id){
        return productoService.findById(id)
            .map(
                p -> ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(p)
            )
//            .defaultIfEmpty(ResponseEntity.internalServerError().build());//CUANDO NO SE ENCUENTRA EL PRODUCTO
            .onErrorResume(ProductoNotFoundException.class, e ->
                Mono.just(ResponseEntity.notFound().build())); //ERROR POR MEDIO DE LA EXCEPTION PERSONALIZADA
    }

    /**
     * ESTABLECIENDO VALIDACION EN EL REQUEST
     * @param monoProducto
     * @return
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {
        Map<String, Object> response = new HashMap<>();

        return monoProducto
                .flatMap(producto -> productoService.save(producto)
                        .map(p -> {
                            response.put("producto", p);
                            response.put("mensaje", "Producto creado con éxito");
                            response.put("timestamp", new Date());
                            return ResponseEntity
                                    .created(URI.create("/api/productos/".concat(p.getId())))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(response);
                        }))
                .onErrorResume(t -> {
                    return Mono.just(t).cast(WebExchangeBindException.class)
                        .flatMap(e -> Mono.just(e.getFieldErrors()))
                        .flatMapMany(Flux::fromIterable)
                        .map(fieldError -> "El campo "+fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> {
                            response.put("errors", list);
                            response.put("timestamp", new Date());
                            response.put("status", HttpStatus.BAD_REQUEST.value());
                            return Mono.just(ResponseEntity.badRequest().body(response));
                        });
                });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> editarAll(@RequestBody Producto producto, @PathVariable String id) {
        return productoService.updateAll(producto, id)
            .map(p -> ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p))
            .onErrorResume(ex -> Mono.just(ResponseEntity.notFound().build())); //OTRA MANERA DE ESTABLECER EL ERROR
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<Producto>> editarPartial(@RequestBody Producto producto, @PathVariable String id) {
        return productoService.updatePartial(producto, id)
            .map(p -> ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p))
            .onErrorResume(ex -> Mono.just(ResponseEntity.notFound().build())); //OTRA MANERA DE ESTABLECER EL ERROR
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {
        return productoService.delete(id)
            .flatMap(deleted -> {
                if (deleted) {
                    return Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));  // Si se eliminó, retorna 204 No Content
                } else {
                    return Mono.just(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));  // Si no se encontró, retorna 404 Not Found
                }
            });
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
        return productoService.uploadPhoto(id, file, path)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build()); //ERROR DICIENDO QUE NO SE ENCONTRÓ EL PRODUCTO
    }

    @PostMapping("/v2")
    public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart FilePart file) {
        return productoService.createProductWithPhoto(producto, file, path)
                .map(p -> ResponseEntity
                        .created(URI.create("/api/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p));
    }
}
