package com.springboot_webflux.app.controllers;

import com.springboot_webflux.app.models.documents.Producto;
import com.springboot_webflux.app.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * ESTA CLASE SIRVE SOLO COMO EJEMPLO DE LA IMPLEMENTACIÓN DE UN CONTROLLER CON LOS TEMPLATES,
 * SOLO SE USA PARA VER LAS DIFERENTES FORMAS DE LISTAR UN DOCUMENTO DE MONGO
 */
@Controller
public class ProductoListarController {

	@Autowired
	private ProductoService service;

	private static final Logger log = LoggerFactory.getLogger(ProductoListarController.class);

	@GetMapping({"/listar", "/"})
	public Mono<String> listar(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCase();

		//EN ESTE CASO EL SUBSCRIBE NO ES NECESARIO POR QUE: flujo reactivo (Flux<Producto>) está siendo "consumido"
		// indirectamente por el motor de plantillas (como Thymeleaf, en este caso) cuando renderiza la vista.
//		productos.subscribe(prod -> log.info(prod.getNombre()));

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return Mono.just("listar");
	}

	@GetMapping("/listar-datadriver")
	public String listarDataDriver(Model model) {

		Flux<Producto> productos = service.findAllConNombreUpperCase().delayElements(Duration.ofSeconds(1));

		productos.subscribe(prod -> log.info(prod.getNombre()));

		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}

	@GetMapping("/listar-full")
	public String listarFull(Model model) {

		Flux<Producto> productos = service.findAllConNombreUpperCaseRepeat();

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}

	@GetMapping("/listar-chunked")
	public String listarChunked(Model model) {

		Flux<Producto> productos = service.findAllConNombreUpperCaseRepeat();

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar-chunked";
	}
}
