package com.springboot_webflux_apirest.models.dao;

import com.springboot_webflux_apirest.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{

}
