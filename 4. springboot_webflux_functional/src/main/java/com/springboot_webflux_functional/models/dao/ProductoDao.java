package com.springboot_webflux_functional.models.dao;

import com.springboot_webflux_functional.models.documents.Producto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{

    public Mono<Producto> findByNombre(String nombre);

    @Query("{ 'nombre': ?0 }")
    public Mono<Producto> obtenerPorNombre(String nombre);

}
