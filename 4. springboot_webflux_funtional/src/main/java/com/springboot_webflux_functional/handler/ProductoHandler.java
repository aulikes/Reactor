package com.springboot_webflux_functional.handler;

import com.springboot_webflux_functional.exceptions.ProductoNotFoundException;
import com.springboot_webflux_functional.models.documents.Categoria;
import com.springboot_webflux_functional.models.documents.Producto;
import com.springboot_webflux_functional.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoService service;

    @Value("${config.uploads.path}")
    private String path;

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> listar(ServerRequest request){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request){
        String id = request.pathVariable("id");
        return service.findById(id)
                .flatMap( p -> ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .bodyValue(p))
//                .switchIfEmpty(ServerResponse.notFound().build());
                .onErrorResume(ProductoNotFoundException.class,
                    e -> ServerResponse.notFound().build()); //ERROR POR MEDIO DE LA EXCEPTION PERSONALIZADA
    }


    public Mono<ServerResponse> crear(ServerRequest request){
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        Map<String, Object> response = new HashMap<>();

        return productoMono.flatMap(producto -> {
            Errors errors = new BeanPropertyBindingResult(producto, Producto.class.getName());
            validator.validate(producto, errors);

            if (!errors.hasErrors()) {
                return service.save(producto)
                    .flatMap(savedProducto -> {
                        response.put("producto", savedProducto);
                        response.put("mensaje", "Producto creado con éxito");
                        response.put("timestamp", new Date());
                        return ServerResponse.created(URI.create("/api/v2/productos/".concat(savedProducto.getId())))
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .bodyValue(response);
                    });
            } else {
                return Flux.fromIterable(errors.getFieldErrors())
                    .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(errorList -> {
                        response.put("errors", errorList);
                        response.put("timestamp", new Date());
                        response.put("status", HttpStatus.BAD_REQUEST.value());
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .bodyValue(response);
                    });
            }
        });
    }

    public Mono<ServerResponse> editarComplete(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        return service.updateAll(producto, id)
            .flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .bodyValue(p)
            )
            .onErrorResume(ProductoNotFoundException.class,
                    e -> ServerResponse.notFound().build()); //ERROR POR MEDIO DE LA EXCEPTION PERSONALIZADA;
    }

    public Mono<ServerResponse> editarPartial(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        return service.updatePartial(producto, id)
            .flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .bodyValue(p)
            )
            .onErrorResume(ProductoNotFoundException.class,
                    e -> ServerResponse.notFound().build()); //ERROR POR MEDIO DE LA EXCEPTION PERSONALIZADA;
    }

    public Mono<ServerResponse> eliminar(ServerRequest request){
        String id = request.pathVariable("id");

        return service.delete(id)
            .flatMap(deleted -> {
                if (deleted) {
                    return ServerResponse.noContent().build();  // Si se eliminó, retorna 204 No Content
                } else {
                    return ServerResponse.notFound().build();  // Si no se encontró, retorna 404 Not Found
                }
            });
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");

        return request.multipartData()
                .map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> service.uploadPhoto(id, Mono.just(file), path))
                .flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crearConFoto(ServerRequest request) {
        Mono<Producto> producto = request.multipartData()
            .map(multipart -> {
                Map<String, Part> formData = multipart.toSingleValueMap();
                FormFieldPart nombre = (FormFieldPart) formData.get("nombre");
                FormFieldPart precio = (FormFieldPart) formData.get("precio");
                FormFieldPart categoriaId = (FormFieldPart) formData.get("categoria.id");
                FormFieldPart categoriaNombre = (FormFieldPart) formData.get("categoria.nombre");

            Categoria categoria = new Categoria(categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
        });

        return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart -> service.createProductoWithPhoto(producto, filePart, path))
                .flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .bodyValue(p));
    }
}
