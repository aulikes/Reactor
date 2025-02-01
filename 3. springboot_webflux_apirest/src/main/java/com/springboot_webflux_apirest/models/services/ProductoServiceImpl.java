package com.springboot_webflux_apirest.models.services;

import com.springboot_webflux_apirest.exceptions.ProductoNotFoundException;
import com.springboot_webflux_apirest.models.dao.CategoriaDao;
import com.springboot_webflux_apirest.models.dao.ProductoDao;
import com.springboot_webflux_apirest.models.documents.Categoria;
import com.springboot_webflux_apirest.models.documents.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Date;
import java.util.UUID;

@Service
public class ProductoServiceImpl implements ProductoService {

	@Autowired
	private ProductoDao dao;
	
	@Override
	public Flux<Producto> findAll() {
		return dao.findAll();
	}

	@Override
	public Mono<Producto> findById(String id) {
		return dao.findById(id)
				.switchIfEmpty(Mono.error(new ProductoNotFoundException(id)));
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		producto.setCreateAt(new Date());
		return dao.save(producto);
	}

	@Override
	public Mono<Producto> updateAll(Producto producto, String id) {
		return this.findById(id) // AQUÍ YA SE MANEJA EL ERROR DEL PRODUCTO NO ENTONTRADO
			.flatMap(p -> {
				// Actualiza los campos del producto existente
				p.setNombre(producto.getNombre());
				p.setPrecio(producto.getPrecio());
				p.setCategoria(producto.getCategoria());
				return dao.save(p);
			});
	}

	@Override
	public Mono<Producto> updatePartial(Producto producto, String id) {
		return this.findById(id) // AQUÍ YA SE MANEJA EL ERROR DEL PRODUCTO NO ENTONTRADO
			.flatMap(p -> {
				// Actualiza solo los campos que no son nulos
				if (producto.getNombre() != null) {
					p.setNombre(producto.getNombre());
				}
				if (producto.getPrecio() != 0) {
					p.setPrecio(producto.getPrecio());
				}
				if (producto.getCategoria() != null) {
					p.setCategoria(producto.getCategoria());
				}
				return dao.save(p);
			});
	}

	@Override
	// Método para eliminar un producto por ID
	public Mono<Boolean> delete(String id) {
		return dao.findById(id)
				.flatMap(producto -> dao.delete(producto).then(Mono.just(true)))  // Si se encuentra, lo elimina y devuelve true
				.switchIfEmpty(Mono.just(false));  // Si no se encuentra, devuelve false
	}

	@Override
	//subir una foto a un producto existente
	public Mono<Producto> uploadPhoto(String id, FilePart file, String path) {
		return findById(id).flatMap(p -> {
			String sanitizedFilename = sanitizeFilename(file.filename());
			p.setFoto(UUID.randomUUID().toString() + "-" + sanitizedFilename);

			File destinationFile = new File(path + p.getFoto());
			return file.transferTo(destinationFile).then(save(p));
		});
	}

	@Override
	//crear un producto con foto
	public Mono<Producto> createProductWithPhoto(Producto producto, FilePart file, String path) {
		if (producto.getCreateAt() == null) {
			producto.setCreateAt(new Date());
		}

		String sanitizedFilename = sanitizeFilename(file.filename());
		producto.setFoto(UUID.randomUUID().toString() + "-" + sanitizedFilename);

		File destinationFile = new File(path + producto.getFoto());
		return file.transferTo(destinationFile).then(save(producto));
	}

	private String sanitizeFilename(String filename) {
		return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
	}

}
