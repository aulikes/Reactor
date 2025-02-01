package com.springboot_webflux_apirest.models.services;

import com.springboot_webflux_apirest.models.dao.CategoriaDao;
import com.springboot_webflux_apirest.models.documents.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoriaServiceImpl implements CategoriaService{

    @Autowired
    private CategoriaDao categoriaDao;

    @Override
    public Flux<Categoria> findAllCategoria() {
        return categoriaDao.findAll();
    }

    @Override
    public Mono<Categoria> findCategoriaById(String id) {
        return categoriaDao.findById(id);
    }

    @Override
    public Mono<Categoria> saveCategoria(Categoria categoria) {
        return categoriaDao.save(categoria);
    }
}
