package com.springboot_webflux_apirest.models.services;

import com.springboot_webflux_apirest.models.documents.Categoria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CategoriaService {

    public Flux<Categoria> findAllCategoria();

    public Mono<Categoria> findCategoriaById(String id);

    public Mono<Categoria> saveCategoria(Categoria categoria);
}
