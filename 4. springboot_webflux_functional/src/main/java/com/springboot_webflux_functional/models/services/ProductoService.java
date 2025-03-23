package com.springboot_webflux_functional.models.services;


import com.springboot_webflux_functional.models.documents.Producto;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ProductoService {
	
	public Flux<Producto> findAllProducto();
	
	public Mono<Producto> findProductoById(String id);

	public Mono<Producto> save(Producto producto);

	public Mono<Producto> updateAll(Mono<Producto> productoReq, String id);

	public Mono<Producto> updatePartial(Mono<Producto> productoReq, String id);

	public Mono<Boolean> delete(String id);

	public Mono<Producto> uploadPhoto(String id, Mono<FilePart> filePartMono, String path);

	public Mono<Producto> createProductoWithPhoto(Mono<Producto> productoMono, FilePart filePart, String path);

	public Mono<Producto> findByNombre(String nombre);

	public Mono<Void> deleteAll();

}
