package com.springboot_webflux_apirest.models.services;


import com.springboot_webflux_apirest.models.documents.Categoria;
import com.springboot_webflux_apirest.models.documents.Producto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {
	
	public Flux<Producto> findAll();
	
	public Mono<Producto> findById(String id);

	public Mono<Producto> save(Producto producto);

	public Mono<Producto> updateAll(Producto producto, String id);

	public Mono<Producto> updatePartial(Producto producto, String id);

	public Mono<Boolean> delete(String id);

	public Mono<Producto> uploadPhoto(String id, FilePart file, String path);

	public Mono<Producto> createProductWithPhoto(Producto producto, FilePart file, String path);

}
