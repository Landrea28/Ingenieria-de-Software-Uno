# Consultas, estado de antecedentes y reporte PDF

Documento de apoyo del módulo de datos. Explica, con el código real del backend, cómo funcionan las consultas con Spring Data JPA, cómo se modela y se consulta el estado de antecedentes, y cómo se genera el PDF.

## 1. Consultas con Spring Data JPA

Hay dos formas de consulta en el proyecto.

**Consultas derivadas del nombre del método.** Spring Data arma la consulta SQL leyendo el nombre. Están en `CiudadanoRepository`:

| Método | Qué hace |
|---|---|
| `findByIdentificacion(id)` | Busca un ciudadano por su identificación |
| `existsByIdentificacion(id)` | Responde si ya existe (se usa para detectar duplicados) |
| `findByIdentificacionContainingIgnoreCase...OrNombres...OrApellidos...(…, pageable)` | Búsqueda parcial, sin distinguir mayúsculas, por identificación, nombres o apellidos, con paginación |

**Consultas JPQL con `@Query`.** Están en `AntecedenteRepository`:

```java
@Query("select a from Antecedente a join fetch a.ciudadano c where c.identificacion = :identificacion")
Optional<Antecedente> buscarPorIdentificacion(String identificacion);

@Query("select a from Antecedente a join fetch a.ciudadano c where a.estado = :estado order by c.apellidos, c.nombres")
List<Antecedente> listarPorEstado(EstadoAntecedente estado);
```

JPQL consulta **entidades** (`Antecedente`, `Ciudadano`), no tablas. Se usa aquí porque la consulta cruza dos entidades. `join fetch` trae el ciudadano en la misma consulta. Sin él, como la relación `Antecedente → Ciudadano` es `LAZY`, cada fila del reporte dispararía una consulta extra (el problema «N+1»).

## 2. Cómo se modela el estado de antecedentes

- Tabla `antecedente`, una fila por ciudadano: `ciudadano_id` es **UNIQUE** y clave foránea a `ciudadano(id)`.
- `estado` solo admite `REQUERIDO` o `NO_REQUERIDO`: lo garantizan el `CHECK` de la base y el enum `EstadoAntecedente` de Java (`@Enumerated(EnumType.STRING)`).
- `fuente` es `SIMULADA`: declara que el dato es de prueba. No existe una fuente oficial.
- Un ciudadano **sin fila** en `antecedente` significa «sin información». `AntecedenteService.consultar` lanza `NegocioException` con 404 y código `SIN_INFORMACION`. Nunca se confunde con NO_REQUERIDO. La identificación inexistente y el ciudadano sin estado dan la misma respuesta.
- `establecer` crea el antecedente si no existe, o cambia el estado y la fecha si ya existe. Si el ciudadano no existe, responde 404 `NO_ENCONTRADO`.

## 3. Claves y restricciones

| Decisión | Motivo |
|---|---|
| `ciudadano.identificacion` UNIQUE | Una persona no se registra dos veces |
| `usuario.identificacion` y `usuario.correo` UNIQUE | Un usuario por persona y un correo por usuario |
| `antecedente.ciudadano_id` UNIQUE | Un solo estado vigente por ciudadano |
| CHECK en `estado`, `rh`, `grupo_sanguineo`, `estatura` y fechas | La base rechaza datos inválidos aunque falle la validación de la aplicación |
| `rango` UNIQUE `(fuerza_codigo, nombre)` | Un rango no se repite dentro de una fuerza |
| Claves compuestas en `usuario_perfil`, `perfil_permiso`, `opcion_permiso` | Evitan asignar dos veces lo mismo |

**Duplicados, en dos capas.** `CiudadanoService.crear` primero llama a `existsByIdentificacion`. Si dos peticiones llegan a la vez, la restricción UNIQUE de la base lanza `DataIntegrityViolationException`, que se captura. En ambos casos la API responde 409 con código `DUPLICADO`.

## 4. Cómo se genera el PDF

Flujo de `GET /api/reportes/ciudadanos?estado=REQUERIDO`:

1. `ReporteController` recibe el filtro `estado`. Solo el perfil `ADMINISTRADOR` puede llamarlo (`@PreAuthorize`).
2. `ReporteService.generar` llama a `listarPorEstado(estado)`, que devuelve los ciudadanos con ese estado, ordenados por apellidos y nombres.
3. Si la lista está vacía, responde 404 con código `SIN_RESULTADOS`. No genera un PDF vacío.
4. Si hay datos, usa la librería **OpenPDF** (`com.lowagie.text`) para crear el documento: título, línea «Reporte de ciudadanos: REQUERIDO», fecha de generación, la leyenda «Datos ficticios de demostracion» y una tabla con **Identificacion, Nombres, Apellidos**.
5. El controlador devuelve el PDF con `Content-Type: application/pdf` y `Content-Disposition: attachment; filename=ciudadanos-requerido.pdf`.

## 5. Pruebas que cubren esto

| Prueba | Qué comprueba |
|---|---|
| `CiudadanoRepositoryTest` | 20 ciudadanos cargados, búsqueda por nombre, y que `9000000020` no tiene antecedente |
| `AntecedenteServiceTest` | Un ciudadano sin estado produce `SIN_INFORMACION` |
| `CiudadanoServiceTest` | Un duplicado produce `DUPLICADO` |
| `ReporteServiceTest` | El resultado es un PDF real (empieza con `%PDF`) y el caso sin resultados lanza `SIN_RESULTADOS` |
| `SeguridadIntegracionTest` | Recorre el endpoint `/api/reportes/ciudadanos` con distintos tokens y roles |

En los archivos que revisé no vi una prueba que verifique los encabezados HTTP del PDF (`Content-Type` y nombre de descarga). Se puede comprobar a mano con la guía de evidencia de abajo.

## 6. Evidencia manual del PDF

Con la aplicación arrancada (ver `reconstruccion-base.md`), en PowerShell:

```powershell
$body = '{"identificacion":"1000000001","contrasena":"Una-clave-de-prueba-1","recordarContrasena":false}'
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login -ContentType "application/json" -Body $body
Invoke-WebRequest -Uri "http://localhost:8080/api/reportes/ciudadanos?estado=REQUERIDO" `
  -Headers @{ Authorization = "Bearer $($login.token)" } -OutFile reporte-requerido.pdf
```

Usa la identificación y la contraseña que definiste en `ADMIN_INITIAL_*`. Abre `reporte-requerido.pdf` y guárdalo como evidencia. Repite con `estado=NO_REQUERIDO`.

## 7. Preguntas de sustentación y respuestas cortas

- **¿Por qué esas claves y restricciones?** Para que la base garantice la integridad aunque falle la aplicación: identificaciones únicas, un estado por ciudadano y valores válidos por `CHECK`.
- **¿Cómo se modela el estado de antecedentes?** Tabla `antecedente` con un registro por ciudadano y un estado limitado a dos valores. Sin registro significa «sin información» (404), nunca NO_REQUERIDO.
- **¿Cómo funciona una consulta con Spring Data JPA?** Las simples se derivan del nombre del método. Las que cruzan entidades usan JPQL con `@Query`, y `join fetch` evita consultas extra.
- **¿Cómo se genera el PDF y cómo se aplica el filtro?** El filtro `estado` llega como parámetro y se pasa a `listarPorEstado`. Con los resultados se arma una tabla con OpenPDF. Sin resultados responde 404.
- **¿Cómo se reconstruye la base desde cero?** `docker compose down -v`, `docker compose up -d` y arrancar la aplicación: Flyway aplica V1 a V7 (y V100 y V101 con el perfil `demo`).
- **¿De dónde sale Requerido / No requerido?** Son datos de prueba. La columna `fuente` lo declara con `SIMULADA`.
