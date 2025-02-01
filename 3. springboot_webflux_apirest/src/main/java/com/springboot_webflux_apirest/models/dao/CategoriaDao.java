package com.springboot_webflux_apirest.models.dao;

import com.springboot_webflux_apirest.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

}
