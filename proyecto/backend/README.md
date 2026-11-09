# Sistema de Consulta Ciudadano — Backend

API REST del sistema de consulta ciudadana. Java 21, Spring Boot (Spring MVC, Tomcat embebido), Maven, PostgreSQL, Spring Data JPA (Hibernate), Flyway, Lombok y contrato OpenAPI en YAML. Arquitectura por capas: controlador → servicio → repositorio → base de datos.

## Estado de implementación

| Módulo | Estado |
|---|---|
| Ciudadanos: crear, listar, buscar, consultar, actualizar | Implementado |
| Antecedentes: consulta y carga de estado ficticio | Implementado |
| Fuerzas y rangos (catálogo) | Implementado |
| Entidades, repositorios y migraciones | Implementado |
| Reporte PDF por estado | Implementado |
| Registro de personal, login y logout | Implementado con validación SIMULADA, BCrypt, JWT y revocación |
| Usuarios, perfiles y permisos | Implementado: CRUD sin DELETE, estado INACTIVO, asignación de roles y reemplazo de permisos |
| Seguridad de rutas | Implementada: autorización por URL, DTOs públicos sin hash y revocación por versión de seguridad |

**No utilizar con datos reales ni exponer a Internet o a una red compartida.** El servidor escucha por defecto solo en `127.0.0.1`.

## Requisitos

- JDK 21
- Maven 3.9 o posterior
- PostgreSQL 15+ o Docker con Docker Compose

## Ejecutar

1. Copiar `.env.example` a `.env`, definir un `DB_PASSWORD` local y arrancar PostgreSQL: `docker compose up -d`.
2. Exportar las variables de `.env.example` a la consola. Maven no carga `.env` automáticamente. Definir un `JWT_SECRET` aleatorio de al menos 32 bytes. Para crear el administrador inicial, configurar `ADMIN_INITIAL_IDENTIFICACION`, `ADMIN_INITIAL_EMAIL` y `ADMIN_INITIAL_PASSWORD`.
3. Ejecutar `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.
4. Abrir `http://localhost:8080/swagger-ui.html` y usar **Try it out**. Swagger y actuator solo se habilitan con el perfil `dev`.

Las migraciones SQL se aplican al arrancar. El servidor es Tomcat embebido: `mvn package` genera un JAR ejecutable en `target/`. El archivo `.env` local está ignorado por Git y no debe versionarse.

## Datos de demostración

`docker compose up -d` inicia PostgreSQL y Mailpit (interfaz en `http://localhost:8025`). `mvn spring-boot:run -Dspring-boot.run.profiles=demo,dev` carga 20 ciudadanos, 19 antecedentes y diez identidades **SIMULADAS** de personal distribuidas en cuatro fuerzas. El ciudadano `9000000020` no tiene estado a propósito y produce 404 `SIN_INFORMACION`. Usar el perfil `demo` solo sobre una base limpia.

Sin `demo` se cargan únicamente el esquema, los catálogos de fuerzas y rangos, y los perfiles y permisos básicos. Todos los estados de antecedentes son ficticios.

## Endpoints

| Operación | Método y ruta |
|---|---|
| Salud | GET `/api/health` |
| Listar y buscar ciudadanos | GET `/api/ciudadanos?busqueda=&pagina=0&tamano=20` |
| Registrar ciudadano | POST `/api/ciudadanos` |
| Consultar ciudadano | GET `/api/ciudadanos/{identificacion}` |
| Actualizar ciudadano | PUT `/api/ciudadanos/{identificacion}` |
| Consultar estado | GET `/api/antecedentes/{identificacion}` |
| Establecer estado de demo | PUT `/api/antecedentes/{identificacion}/estado` |
| Catálogo de fuerzas y rangos | GET `/api/catalogos/fuerzas-rangos` |
| Reporte PDF | GET `/api/reportes/ciudadanos?estado=REQUERIDO` |
| Registro de personal | POST `/api/personal/registro` (público; el correo SIMULADO usa SMTP local) |
| Login / logout | POST `/api/auth/login` (público), POST `/api/auth/logout` (requiere JWT) |
| Usuarios | GET/POST `/api/usuarios`, GET/PUT `/api/usuarios/{identificacion}` (solo ADMINISTRADOR) |
| Perfiles y permisos | GET `/api/perfiles`, GET `/api/permisos`, GET/PUT `/api/perfiles/{id}/permisos` (solo ADMINISTRADOR) |

`JWT_SECRET` firma los tokens; `recordarContrasena` aumenta su expiración, no guarda la contraseña. El sexto fallo de login bloquea la identificación por 15 minutos. El registro limita a cinco intentos por IP cada 15 minutos. Ambos controles se persisten en PostgreSQL. `CORS_ALLOWED_ORIGINS` acepta una lista explícita de orígenes, nunca `*`.

Variables principales: `JWT_SECRET`, `ADMIN_INITIAL_*`, `MAIL_*` y `CORS_ALLOWED_ORIGINS`. No guardes valores reales en el repositorio. Defensa y el correo están etiquetados como **SIMULADOS**.

## Documentación y calidad

- Contrato OpenAPI: `src/main/resources/static/openapi/openapi.yaml`; operaciones en `openapi/paths`, esquemas en `openapi/schemas`. Los controladores no llevan anotaciones de documentación.
- `mvn test` ejecuta pruebas JUnit, Mockito, MockMvc, persistencia con H2 y reglas de arquitectura con ArchUnit.
- `mvn clean package` valida el empaquetado completo.
- [Pruebas Postman/Newman](docs/pruebas/README.md): `newman run docs/pruebas/postman/seguridad.postman_collection.json -e docs/pruebas/postman/seguridad.postman_environment.json`.
- [Plan de pruebas](docs/pruebas/plan-de-pruebas.md) y [resultados medidos de Surefire](docs/pruebas/resultados-seguridad.md).
- Más detalle en `docs/`: arquitectura, requisitos, especificación de clases y supuestos.
