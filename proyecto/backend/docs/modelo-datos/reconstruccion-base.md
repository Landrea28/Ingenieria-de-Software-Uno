# Reconstrucción de la base de datos desde cero

Esta guía crea la base de datos PostgreSQL del Sistema de Consulta Ciudadano desde una instalación limpia y comprueba que quedó bien. Todos los datos de demostración son **ficticios**.

## Cómo se construye la base

El esquema lo crea **Flyway** al arrancar la aplicación. No hay que ejecutar scripts a mano.

| Origen | Qué crea | Cuándo se ejecuta |
|---|---|---|
| `db/migration/V1` a `V7` | Tablas, restricciones, índices, catálogos (fuerzas, rangos), perfiles, permisos, opciones y sus relaciones | Siempre |
| `db/demo/V100` y `V101` | 20 ciudadanos, 19 antecedentes y 10 integrantes simulados de personal | Solo con el perfil `demo` |

Hibernate está en modo `ddl-auto: validate`: no crea ni modifica tablas, solo comprueba que las entidades JPA coincidan con el esquema creado por Flyway. Si algo no coincide, la aplicación no arranca.

## Requisitos

- JDK 21
- Maven 3.9 o posterior
- Docker con Docker Compose (o un PostgreSQL 15+ instalado y una base vacía llamada `consulta_ciudadano`)

## Pasos (Windows PowerShell, desde `proyecto/backend`)

1. **Crear el archivo de variables.** Copia `.env.example` a `.env` y escribe un valor en `DB_PASSWORD`. Ese archivo está ignorado por Git y no se sube al repositorio.

   ```powershell
   Copy-Item .env.example .env
   (Get-Content .env) -replace '^DB_PASSWORD=.*','DB_PASSWORD=ClaveLocal123' | Set-Content .env
   ```

   La segunda línea escribe la clave en `.env` (también puedes editar el archivo a mano). Ejecuta cada comando por separado. Si `docker compose up` dice `required variable DB_PASSWORD is missing`, esa línea quedó vacía.

2. **Levantar PostgreSQL** (y Mailpit, el correo de pruebas):

   ```powershell
   docker compose up -d
   ```

3. **Definir las variables en la consola.** Maven no lee el archivo `.env`, así que se definen a mano. Usa valores propios, nunca reales ni los subas al repositorio.

   ```powershell
   $env:DB_PASSWORD = "la-misma-clave-del-env"
   $env:JWT_SECRET = "una-cadena-aleatoria-de-al-menos-32-caracteres"
   $env:ADMIN_INITIAL_IDENTIFICACION = "1000000001"
   $env:ADMIN_INITIAL_EMAIL = "admin@ejemplo.local"
   $env:ADMIN_INITIAL_PASSWORD = "Una-clave-de-prueba-1"
   ```

   - `JWT_SECRET` es obligatorio: sin él la aplicación no arranca.
   - Las tres variables `ADMIN_INITIAL_*` crean el administrador inicial. Su contraseña se guarda con hash. Si no las defines, no se crea administrador.

4. **Arrancar con datos de demostración.** En PowerShell las comillas son necesarias porque la coma separa argumentos:

   ```powershell
   mvn spring-boot:run "-Dspring-boot.run.profiles=demo,dev"
   ```

   Si usas Git Bash, Linux o macOS: `mvn spring-boot:run -Dspring-boot.run.profiles=demo,dev`. Para crear **solo** el esquema y los catálogos, sin datos de ejemplo, quita `demo` y deja `dev`.

5. **Comprobar** que quedó bien (en otra consola, también desde `proyecto/backend`):

   ```powershell
   docker compose exec postgres psql -U consulta -d consulta_ciudadano -c "\dt"
   docker compose exec postgres psql -U consulta -d consulta_ciudadano -c "select count(*) from ciudadano;"
   docker compose exec postgres psql -U consulta -d consulta_ciudadano -c "select count(*) from antecedente;"
   docker compose exec postgres psql -U consulta -d consulta_ciudadano -c "select count(*) from defensa_personal_simulado;"
   docker compose exec postgres psql -U consulta -d consulta_ciudadano -c "select version, description, success from flyway_schema_history order by installed_rank;"
   ```

   Resultado esperado con el perfil `demo`:

   | Comprobación | Valor |
   |---|---|
   | Tablas de la aplicación | 15, más `flyway_schema_history` |
   | `ciudadano` | 20 filas |
   | `antecedente` | 19 filas (el ciudadano `9000000020` no tiene, a propósito) |
   | `defensa_personal_simulado` | 10 filas |
   | `flyway_schema_history` | 9 migraciones (V1 a V7, V100 y V101), todas con `success = t` |

## Volver a empezar desde cero

Para borrar la base y reconstruirla:

```powershell
docker compose down -v
docker compose up -d
```

`-v` elimina el volumen con los datos. Después repite los pasos 3 y 4. **El perfil `demo` solo se usa sobre una base limpia**, como indica el README del backend.

## Problemas frecuentes

- **`mvn` no se reconoce como comando.** Se descargó el archivo `-src` en lugar del `-bin`. Descarga `apache-maven-<versión>-bin.zip`, descomprímelo y agrega su carpeta `bin` al `Path` de la consola.
- **`ExceptionInInitializerError ... EndPosTable` al compilar.** Maven está usando un JDK distinto de 21. Define `JAVA_HOME` con la ruta del JDK 21 y antepón su `bin` al `Path` antes de ejecutar Maven.
- **`required variable DB_PASSWORD is missing`.** El archivo `.env` no tiene valor en `DB_PASSWORD` (paso 1).
- **La aplicación no conecta con la base.** La clave de `$env:DB_PASSWORD` debe ser igual a la del `.env`. Si se cambió después de crear el volumen, ejecuta `docker compose down -v` y vuelve a empezar.

## Reglas para no romper la base

- **No edites una migración que ya se aplicó** (V1 a V7). Flyway guarda una huella de cada una y falla si cambia. Los cambios van en una migración nueva (`V8__...sql`).
- Los datos de `db/demo` nunca se mezclan con las migraciones reales.
- No subas contraseñas, `JWT_SECRET` ni el archivo `.env` al repositorio.

## Pruebas automáticas sin Docker

Las pruebas usan una base H2 en memoria en modo PostgreSQL, con las mismas migraciones y los datos de demostración. No necesitan Docker ni PostgreSQL:

```powershell
mvn test
```

Guarda la salida (o una captura) como evidencia de las pruebas de persistencia y del reporte.

## De dónde sale el estado Requerido / No requerido

No existe una fuente oficial. Los estados de la tabla `antecedente` son **datos de prueba**, y la columna `fuente` lo declara con el valor `SIMULADA`. Un ciudadano sin fila en `antecedente` significa «sin información» (la API responde 404), no NO_REQUERIDO.
