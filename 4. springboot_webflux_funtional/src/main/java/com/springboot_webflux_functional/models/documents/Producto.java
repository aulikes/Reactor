package com.springboot_webflux_functional.models.documents;

import java.util.Date;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

@Document(collection="productos")
@Data
public class Producto {
	
	@Id
	private String id;
	
	@NotEmpty
	private String nombre;
	
	@NotNull
	private Double precio;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createAt;
	
	@Valid
	private Categoria categoria;
	
	private String foto;

	public Producto() {}

	public Producto(String nombre, Double precio) {
		this.nombre = nombre;
		this.precio = precio;
	}
	
	public Producto(String nombre, Double precio, Categoria categoria) {
		this(nombre, precio);
		this.categoria = categoria;
	}

}
