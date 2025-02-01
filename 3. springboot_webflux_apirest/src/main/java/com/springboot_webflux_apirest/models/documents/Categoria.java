package com.springboot_webflux_apirest.models.documents;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categorias")
@Data
public class Categoria {
	
	@Id
	@NotEmpty
	private String id;
	
	private String nombre;
	
	public Categoria() {
	}

	public Categoria(String nombre) {
		this.nombre = nombre;
	}
}
