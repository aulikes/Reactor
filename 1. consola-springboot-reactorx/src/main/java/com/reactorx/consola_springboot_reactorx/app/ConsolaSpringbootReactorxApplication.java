package com.reactorx.consola_springboot_reactorx.app;

import com.reactorx.consola_springboot_reactorx.app.models.Comentarios;
import com.reactorx.consola_springboot_reactorx.app.models.Usuario;
import com.reactorx.consola_springboot_reactorx.app.models.UsuarioComentarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ConsolaSpringbootReactorxApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConsolaSpringbootReactorxApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConsolaSpringbootReactorxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//		ejemplo0_FluxJust(); //Obtener un observable desde un Array String y procesarlo luego en un suscriptor
//		ejemplo1_FluxJust(); //Obtener un observable desde un Array String, Pasarlo a un Observable de Usuario y procesarlo luego en un suscriptor
//		ejemplo2_FluxFromIterable(); //Obtener un observable desde una List y procesarlo luego en un suscriptor
//		ejemplo3_FluxFromIterable_toMono(); //Obtener un observable desde una List y lo transforma en un MONO, que luego es procesado en un suscriptor uno a uno
		ejemplo4_Mono_UsuarioComentariosFlatMap(); //Se establecebe un MONO obsevable de Usuario_con_Comentarios, y el mismo tiene su debido suscriptor ME ENCANTAAAAAAAAAAAAAA
		ejemplo5_Mono_UsuarioComentariosZipWith(); //igual que ejemplo4_Mono_UsuarioComentariosFlatMap, pero con ZipWith
//		ejemploZipWithRangos(); //solo es de prueba
//		ejemploInterval();//bloqueados en X segundos
//		ejemploBlockLastElements();
    }

	private void ejemplo0_FluxJust(){
		log.info("ejemplo0_FluxJust - INICIANDO!");
		//Flux es una clase que implementa un Publisher y como tal es un Observable en este caso
		Flux<String> nombres = Flux.just("Andrés", "Pedro", "Diego", "Juan")
			.doOnNext(elemento -> {
				if (elemento.isEmpty()){
					throw new RuntimeException("Nombres no pueden ser vacíos");
				}
				log.info("------------------------------ Se hace en doOnNext: nombre Real: " + elemento);
			}).map(nombre -> {
				return nombre.toUpperCase();
			});

		nombres.subscribe(e -> log.info(e),
			error -> log.error(error.getMessage()),
			new Runnable() {
				@Override
				public void run() {
					log.info("ejemplo0_FluxJust - Ha finalizado la ejecución del observable con éxito!");
				}
			});
	}

	private void ejemplo1_FluxJust(){
		log.info("ejemplo1_FluxJust - INICIANDO!");
		Flux<String> nombres = Flux.just("Andres Guzman", "Pedro Fulano", "Maria Fulana", "Diego Sultano", "Juan Mengano", "Bruce Lee", "Bruce Willis");
		Flux<Usuario> usuarios = nombres.map(
				nombre -> new Usuario(
						nombre.split(" ")[0].toUpperCase(),
						nombre.split(" ")[1].toUpperCase()))
			.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
			.doOnNext(usuario -> {
				if (usuario == null) {
					throw new RuntimeException("Nombres no pueden ser vacíos");
				}
				log.info("------------------------------ Se hace en doOnNext: " + usuario.getNombre().concat(" ").concat(usuario.getApellido()));
			})
			.map(usuario -> {
				String nombre = usuario.getNombre().toLowerCase();
				usuario.setNombre(nombre);
				return usuario;
			});

		usuarios.subscribe(e -> log.info(e.toString()),
			error -> log.error(error.getMessage()),
			new Runnable() {
				@Override
				public void run() {
					log.info("ejemplo1_FluxJust - Ha finalizado la ejecución del observable con éxito!");
				}
			});
	}

	private void ejemplo2_FluxFromIterable(){
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Fulana");
		usuariosList.add("Diego Sultano");
		usuariosList.add("Pedro Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		log.info("ejemplo2_FluxFromIterable - INICIANDO!");
		Flux<String> nombres = Flux.fromIterable(usuariosList);
		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
			.filter(usuario -> usuario.getNombre().equalsIgnoreCase("Pedro"))
			.doOnNext(usuario -> {
				if(usuario == null) {
					throw new RuntimeException("Nombres no pueden ser vacíos");
				}
				log.info("------------------------------ Se hace en doOnNext: " + usuario.getNombre().concat(" ").concat(usuario.getApellido()));
			})
			.map(usuario -> {
				String nombre =  usuario.getNombre().toLowerCase();
				usuario.setNombre(nombre);
				return usuario;
			});

		usuarios.subscribe(e -> log.info(e.toString()),
			error -> log.error(error.getMessage()),
			new Runnable() {
				@Override
				public void run() {
					log.info("ejemplo2_FluxFromIterable - Ha finalizado la ejecución del observable con éxito!");
				}
			});
	}

	private void ejemplo3_FluxFromIterable_toMono() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Fulana");
		usuariosList.add("Diego Sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		log.info("ejemplo3_FluxFromIterable_toMono - INICIANDO!");
		Flux.fromIterable(usuariosList)
			.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
			.flatMap(usuario -> {
				//Se hacen filtros con los que se puede determinar el tipo de dato que va a usar el subscriptor, EN ESTE CASO SOLO SE USA USUARIO
				if (usuario.getNombre().equalsIgnoreCase("bruce")) {
					return Mono.just(usuario); //Agrega este valor al subscriptor
				} else {
					return Mono.empty(); //Emite el valor y no lo agrega al subscriptor
				}
			}).map(usuario -> {
				String nombre = usuario.getNombre().toLowerCase();
				usuario.setNombre(nombre);
				return usuario;
			}).subscribe(
				usuario -> log.info("Usuario procesado: " + usuario), // onNext: Procesar usuario encontrado
				error -> log.error("Error en el flujo: " + error),     // onError: Manejo de errores
				() -> log.info("ejemplo3_FluxFromIterable_toMono - Flujo completado sin más usuarios.")  // onComplete: Acción al completar el flujo
			);
	}

	private void ejemplo4_Mono_UsuarioComentariosFlatMap() {
		log.info("ejemplo4_Mono_UsuarioComentariosFlatMap - INICIANDO!");
		//FlatMap combina dos flujos
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c)));

		usuarioConComentarios.subscribe(
			uc -> log.info("Usuario procesado: " + uc.toString()), // onNext: Procesar usuario encontrado
			error -> log.error("Error en el flujo: " + error),     // onError: Manejo de errores
			() -> log.info("ejemplo4_Mono_UsuarioComentariosFlatMap - Flujo completado sin más usuarios.")  // onComplete: Acción al completar el flujo
		);
	}

	private void ejemplo5_Mono_UsuarioComentariosZipWith() {
		log.info("ejemplo5_Mono_UsuarioComentariosZipWith - INICIANDO!");
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono,
				(usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario));

		usuarioConComentarios.subscribe(
			uc -> log.info("Usuario procesado: " + uc.toString()), // onNext: Procesar usuario encontrado
			error -> log.error("Error en el flujo: " + error),     // onError: Manejo de errores
			() -> log.info("ejemplo5_Mono_UsuarioComentariosZipWith - Flujo completado sin más usuarios.")  // onComplete: Acción al completar el flujo
		);
	}

	private void ejemploZipWithRangos() {
		Flux<Integer> rangos = Flux.range(0, 4);
		Flux.just(1, 2, 3, 4).map(i -> (i * 2))
				.zipWith(rangos, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(texto -> log.info(texto));
	}

	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		rango.zipWith(retraso, (ra, re) -> ra).doOnNext(i -> log.info(i.toString())).blockLast();
	}

	public void ejemploBlockLastElements() {
		Flux<Integer> rango = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));
		rango.blockLast();
	}
}
