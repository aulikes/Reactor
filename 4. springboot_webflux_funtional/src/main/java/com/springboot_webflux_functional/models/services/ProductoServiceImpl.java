package com.springboot_webflux_functional.models.services;

import com.springboot_webflux_functional.exceptions.ProductoNotFoundException;
import com.springboot_webflux_functional.models.dao.ProductoDao;
import com.springboot_webflux_functional.models.documents.Categoria;
import com.springboot_webflux_functional.models.documents.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Date;
import java.util.Map;
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
	public Mono<Producto> updateAll(Mono<Producto> productoReq, String id) {

		Mono<Producto> productoDb = this.findById(id); // AQUÍ YA SE MANEJA EL ERROR DEL PRODUCTO NO ENCONTRADO

		return productoDb.zipWith(productoReq, (db, req) -> {//bd es productoDb, req es productoReq
			db.setNombre(req.getNombre());
			db.setPrecio(req.getPrecio());
			db.setCategoria(req.getCategoria());
			return db;
		}).flatMap(bd -> dao.save(bd)); // Guarda el producto actualizado y devuelve el resultado
	}

	@Override
	public Mono<Producto> updatePartial(Mono<Producto> productoReq, String id) {

		Mono<Producto> productoDb = this.findById(id); // AQUÍ YA SE MANEJA EL ERROR DEL PRODUCTO NO ENCONTRADO

		return productoDb.zipWith(productoReq, (db, req) -> {//bd es productoDb, req es productoReq
			// Actualiza solo los campos que no son nulos
			if (req.getNombre() != null) {
				db.setNombre(req.getNombre());
			}
			if (req.getPrecio() != 0) {
				db.setPrecio(req.getPrecio());
			}
			if (req.getCategoria() != null) {
				db.setCategoria(req.getCategoria());
			}
			return db;
		}).flatMap(bd -> dao.save(bd)); // Guarda el producto actualizado y devuelve el resultado
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
	public Mono<Producto> uploadPhoto(String id, Mono<FilePart> filePartMono, String path) {
		return findById(id) // AQUÍ YA SE MANEJA EL ERROR DEL PRODUCTO NO ENCONTRADO
			.flatMap(productoBD -> filePartMono.flatMap(file -> {
				String sanitizedFilename = sanitizeFilename(file.filename());
				productoBD.setFoto(UUID.randomUUID().toString() + "-" + sanitizedFilename);

				File destinationFile = new File(path + productoBD.getFoto());
				return file.transferTo(destinationFile) //una vez subida la imagen guardamos el producto con THEN
						.then(save(productoBD));
			}));
	}

	@Override
	// Método para manejar la transferencia del archivo y guardar el producto
	public Mono<Producto> createProductoWithPhoto(Mono<Producto> productoMono, FilePart filePart, String path) {
		return productoMono
			.flatMap(producto -> {
				String fileName = sanitizeFilename(filePart.filename());
				producto.setFoto(fileName);

				return filePart.transferTo(new File(path + producto.getFoto()))
						.then(save(producto));
		});
	}

	@Override
	public Mono<Producto> findByNombre(String nombre) {
		return dao.obtenerPorNombre(nombre);
	}


	private String sanitizeFilename(String filename) {
		return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
	}

}
