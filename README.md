# Eventos
1. consola-springboot-reactorx
Creado en base al curso https://www.udemy.com/course/programacion-reactiva-con-spring-webflux-reactor/learn/lecture/15847718#overview
RxJava es la implementación de ReactiveX para Java. En este curso se hace uso ReactiveX. 
ReactiveX es una API que facilita el manejo de flujos de datos y eventos, a partir de una combinación de el patrón Observer, el patrón Iterator, y características de la Programación Funcional.
El manejo de datos en tiempo real es una tarea común en el desarrollo de aplicaciones. Por lo tanto, tener una manera eficiente y limpia de lidiar con esta tarea es muy importante.
ReactiveX (mediante el uso de Observables y operadores) nos ofrece una API flexible para crear y actuar sobre los flujos de datos. Además, simplifica la programación asíncrona, como la creación de hilos y los problemas de concurrencia.


2. springboot-webflux

Primero, debemos levantar la base de datos de MONGO, con el script de conexión que aparece en: application.properties
La URL principal es http://localhost:8080/listar

Spring WebFlux:
	1.	Recibe el Flux<Producto> como la respuesta del controlador.
	2.	Actúa como un intermediario entre el servidor y el cliente HTTP.
	3.	Automáticamente suscribe al flujo reactivo cuando un cliente realiza la solicitud HTTP.
	4.	Los datos son enviados de manera reactiva al cliente mientras se procesan.
Esto significa que Spring WebFlux se encarga de manejar la suscripción para enviar los datos al cliente, y no necesitas invocar manualmente subscribe.
Si llamaras a subscribe manualmente en el controlador, consumirías el flujo dentro del servidor. Como resultado, el Flux<Producto> ya no estaría disponible para que Spring WebFlux lo procese, lo que podría provocar un error o comportamiento inesperado.

