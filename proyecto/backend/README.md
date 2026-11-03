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
| Seguridad de rutas | Implementada: roles por URL y `@PreAuthorize` |

**No utilizar con datos reales ni exponer a Internet o a una red compartida.** El servidor escucha por defecto solo en `127.0.0.1`.

## Requisitos

- JDK 21
- Maven 3.9 o posterior
- PostgreSQL 15+ o Docker con Docker Compose

## Ejecutar

1. Arrancar PostgreSQL: `docker compose up -d` (o crear la base `consulta_ciudadano` y el usuario `consulta`).
2. Definir las variables de entorno de `.env.example` (incluyendo `JWT_SECRET` de al menos 32 bytes). Maven no carga `.env` automáticamente. Para crear el administrador inicial, configurar también los tres valores `ADMIN_INITIAL_*`.
3. `mvn spring-boot:run`
4. Abrir `http://localhost:8080/swagger-ui.html` y usar **Try it out**.

Las migraciones SQL se aplican al arrancar. El servidor es Tomcat embebido: `mvn package` genera un JAR ejecutable en `target/`.

## Datos de demostración

`docker compose up -d` inicia PostgreSQL y Mailpit (interfaz en `http://localhost:8025`). `mvn spring-boot:run -Dspring-boot.run.profiles=demo` carga 20 ciudadanos, 19 antecedentes y diez identidades **SIMULADAS** de personal distribuidas en cuatro fuerzas. El ciudadano `9000000020` no tiene estado a propósito y produce 404 `SIN_INFORMACION`. Usar el perfil `demo` solo sobre una base limpia.

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
| Registro de personal | POST `/api/personal/registro` (público; el sistema envía credenciales por SMTP) |
| Login / logout | POST `/api/auth/login` (público), POST `/api/auth/logout` (requiere JWT) |

`JWT_SECRET` firma los tokens; `recordarContrasena` aumenta su expiración, no
guarda la contraseña. Tras cinco fallos, la identificación se bloquea por 15
minutos en PostgreSQL, también entre instancias. Ver
`docs/decisiones/seguridad.md` para límites y decisiones.

## Documentación y calidad

- Contrato OpenAPI: `src/main/resources/static/openapi/openapi.yaml`; operaciones en `openapi/paths`, esquemas en `openapi/schemas`. Los controladores no llevan anotaciones de documentación.
- `mvn test` ejecuta pruebas JUnit, Mockito, MockMvc, persistencia con H2 y reglas de arquitectura con ArchUnit.
- Más detalle en `docs/`: arquitectura, requisitos, especificación de clases y supuestos.
