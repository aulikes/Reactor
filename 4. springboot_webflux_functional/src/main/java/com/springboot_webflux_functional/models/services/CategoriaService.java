package com.springboot_webflux_functional.models.services;

import com.springboot_webflux_functional.models.documents.Categoria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CategoriaService {

    public Flux<Categoria> findAllCategoria();

    public Mono<Categoria> findCategoriaById(String id);

    public Mono<Categoria> saveCategoria(Categoria categoria);

    public Mono<Categoria> findByNombre(String nombre);

    public Mono<Void> deleteAll();
}
