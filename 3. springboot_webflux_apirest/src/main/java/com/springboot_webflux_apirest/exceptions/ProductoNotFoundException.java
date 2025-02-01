package com.springboot_webflux_apirest.exceptions;

public class ProductoNotFoundException extends RuntimeException {
    public ProductoNotFoundException(String id) {
        super("Producto no encontrado con id: " + id);
    }
}