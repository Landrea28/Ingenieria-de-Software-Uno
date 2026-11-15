# Diccionario de datos — Sistema de Consulta Ciudadano

Fuente: migraciones Flyway V1 a V7 de `proyecto/backend/src/main/resources/db/migration/`. Motor: PostgreSQL. Los datos de demostración son **ficticios**.

Convención: **Nulo** indica si la columna admite NULL (Sí/No). **Clave**: PK primaria, FK foránea, UK única.

Resumen: 15 tablas. Negocio: `ciudadano`, `antecedente`. Catálogos: `fuerza_publica`, `rango`. Seguridad y usuarios: `usuario`, `perfil`, `permiso`, `opcion`, `usuario_perfil`, `perfil_permiso`, `opcion_permiso`. Simulación: `defensa_personal_simulado`. Apoyo a seguridad: `token_revocado`, `auth_login_intento`, `auth_registro_intento`.

---

## 1. ciudadano

Persona registrada en el sistema.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| id | BIGINT (identidad) | No | PK | | Identificador interno |
| identificacion | VARCHAR(10) | No | UK | Único | Número de identificación (el que trae el código de barras) |
| nombres | VARCHAR(100) | No | | | Nombres |
| apellidos | VARCHAR(100) | No | | | Apellidos |
| fecha_nacimiento | DATE | No | | | Fecha de nacimiento |
| lugar_nacimiento | VARCHAR(150) | No | | | Lugar de nacimiento |
| fecha_expedicion | DATE | No | | CHECK `fecha_expedicion > fecha_nacimiento` | Fecha de expedición del documento |
| lugar_expedicion | VARCHAR(150) | No | | | Lugar de expedición |
| rh | VARCHAR(1) | No | | CHECK `IN ('+','-')` | Factor RH |
| grupo_sanguineo | VARCHAR(2) | No | | CHECK `IN ('A','B','AB','O')` | Grupo sanguíneo |
| estatura | NUMERIC(3,2) | No | | CHECK entre 0.50 y 2.50 | Estatura en metros |

Índices: `ix_ciudadano_apellidos (apellidos, nombres)` para acelerar la búsqueda.

## 2. antecedente

Estado de antecedentes de un ciudadano.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| id | BIGINT (identidad) | No | PK | | Identificador interno |
| ciudadano_id | BIGINT | No | FK, UK | Referencia `ciudadano(id)`; único | Ciudadano al que pertenece (máximo un estado por ciudadano) |
| estado | VARCHAR(20) | No | | CHECK `IN ('REQUERIDO','NO_REQUERIDO')` | Estado de antecedentes |
| fuente | VARCHAR(50) | No | | | Origen del dato. En la demostración es `SIMULADA`, porque no existe una fuente oficial |
| fecha_actualizacion | TIMESTAMP WITH TIME ZONE | No | | | Última actualización del estado |

Índices: `ix_antecedente_estado (estado)` para filtrar el reporte PDF.

Nota: un ciudadano sin fila en esta tabla significa «sin información» (respuesta 404 de la API), no NO_REQUERIDO.

## 3. fuerza_publica

Catálogo de fuerzas. Valores iniciales: POLICIA, EJERCITO, ARMADA, FUERZA_AEREA.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| codigo | VARCHAR(30) | No | PK | | Código de la fuerza (ej. `EJERCITO`) |
| nombre | VARCHAR(70) | No | | | Nombre completo |

## 4. rango

Catálogo de rangos por fuerza (tres por cada fuerza en la carga inicial).

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| id | BIGINT (identidad) | No | PK | | Identificador interno |
| fuerza_codigo | VARCHAR(30) | No | FK | Referencia `fuerza_publica(codigo)` | Fuerza a la que pertenece el rango |
| nombre | VARCHAR(80) | No | | UNIQUE `(fuerza_codigo, nombre)` | Nombre del rango (ej. `Sargento`) |

## 5. usuario

Personal que accede al sistema (registrado o administrador).

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| id | BIGINT (identidad) | No | PK | | Identificador interno |
| identificacion | VARCHAR(10) | No | UK | Único | Identificación del usuario (se usa para el login) |
| nombres | VARCHAR(100) | No | | | Nombres |
| apellidos | VARCHAR(100) | No | | | Apellidos |
| correo | VARCHAR(200) | No | UK | Único | Correo al que se envía la contraseña generada |
| fuerza_codigo | VARCHAR(30) | Sí | FK | Referencia `fuerza_publica(codigo)` | Fuerza del integrante (vacío en el administrador inicial) |
| rango_id | BIGINT | Sí | FK | Referencia `rango(id)` | Rango del integrante |
| id_fuerza | VARCHAR(40) | Sí | | | ID institucional alfanumérico, validado contra el dataset simulado |
| password_hash | VARCHAR(255) | No | | | Contraseña guardada con hash (BCrypt). Nunca en texto plano |
| activo | BOOLEAN | No | | | Estado del usuario (activo/inactivo; no se borra) |
| fecha_creacion | TIMESTAMP WITH TIME ZONE | No | | | Fecha de creación |
| version_seguridad | BIGINT | No | | DEFAULT 0 (agregada en V5) | Versión que sirve para revocar tokens ya emitidos |

Índices: `ix_usuario_fuerza (fuerza_codigo)`.

## 6. perfil

Rol del sistema. Valores iniciales: ADMINISTRADOR, PERSONAL_FUERZA_PUBLICA.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| id | BIGINT (identidad) | No | PK | | Identificador interno |
| codigo | VARCHAR(50) | No | UK | Único | Código del perfil |
| nombre | VARCHAR(100) | No | | | Nombre visible |

## 7. permiso

Acción permitida. Valores iniciales: CIUDADANOS_LEER, CIUDADANOS_CREAR, CIUDADANOS_ACTUALIZAR, ANTECEDENTES_CONSULTAR, ANTECEDENTES_ACTUALIZAR, REPORTES_GENERAR, SEGURIDAD_ADMINISTRAR.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| id | BIGINT (identidad) | No | PK | | Identificador interno |
| codigo | VARCHAR(60) | No | UK | Único | Código del permiso |
| nombre | VARCHAR(100) | No | | | Nombre visible |

## 8. opcion

Opción de menú de la aplicación. Valores iniciales: CIUDADANOS, ANTECEDENTES, REPORTES, SEGURIDAD.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| id | BIGINT (identidad) | No | PK | | Identificador interno |
| codigo | VARCHAR(60) | No | UK | Único | Código de la opción |
| nombre | VARCHAR(100) | No | | | Nombre visible |
| ruta | VARCHAR(150) | No | | | Ruta de la pantalla (ej. `/ciudadanos`) |

## 9. usuario_perfil

Relaciona usuarios con perfiles (muchos a muchos).

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| usuario_id | BIGINT | No | PK, FK | Referencia `usuario(id)` | Usuario |
| perfil_id | BIGINT | No | PK, FK | Referencia `perfil(id)` | Perfil asignado |

Clave primaria compuesta `(usuario_id, perfil_id)`.

## 10. perfil_permiso

Relaciona perfiles con permisos (muchos a muchos). El administrador recibe todos; el personal de fuerza pública recibe ANTECEDENTES_CONSULTAR.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| perfil_id | BIGINT | No | PK, FK | Referencia `perfil(id)` | Perfil |
| permiso_id | BIGINT | No | PK, FK | Referencia `permiso(id)` | Permiso concedido |

Clave primaria compuesta `(perfil_id, permiso_id)`.

## 11. opcion_permiso

Relaciona opciones de menú con los permisos que las habilitan (muchos a muchos).

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| opcion_id | BIGINT | No | PK, FK | Referencia `opcion(id)` | Opción de menú |
| permiso_id | BIGINT | No | PK, FK | Referencia `permiso(id)` | Permiso que la habilita |

Clave primaria compuesta `(opcion_id, permiso_id)`.

## 12. defensa_personal_simulado

Dataset **ficticio** que simula la consulta al Departamento de Defensa al registrar personal. No es una integración real.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| id | BIGINT (identidad) | No | PK | | Identificador interno |
| identificacion | VARCHAR(10) | No | UK | Único | Identificación del integrante simulado |
| nombres | VARCHAR(100) | No | | | Nombres |
| apellidos | VARCHAR(100) | No | | | Apellidos |
| fuerza_codigo | VARCHAR(30) | No | FK | Referencia `fuerza_publica(codigo)` | Fuerza |
| rango_id | BIGINT | No | FK | Referencia `rango(id)` | Rango |
| id_fuerza | VARCHAR(40) | No | | | ID institucional que debe coincidir en el registro |
| activo | BOOLEAN | No | | | Si el integrante está activo en la simulación |

## 13. token_revocado

Tokens JWT invalidados al cerrar sesión.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| jti | VARCHAR(36) | No | PK | | Identificador único del token |
| expira_en | TIMESTAMP WITH TIME ZONE | No | | | Cuándo expira el token (después se puede limpiar el registro) |

Índices: `ix_token_revocado_expira_en (expira_en)`.

## 14. auth_login_intento

Intentos fallidos de login por identificación (control de bloqueo).

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| identificacion | VARCHAR(10) | No | PK | | Identificación que intentó entrar |
| fallos | INTEGER | No | | CHECK entre 1 y 6 (ampliado en V6) | Número de fallos acumulados |
| ultimo_intento | TIMESTAMP WITH TIME ZONE | No | | | Fecha del último intento |
| bloqueado_hasta | TIMESTAMP WITH TIME ZONE | Sí | | | Hasta cuándo está bloqueada la identificación |

Según el README del backend, el sexto fallo bloquea la identificación por 15 minutos. Índices: `ix_auth_login_bloqueo (bloqueado_hasta)`.

## 15. auth_registro_intento

Control del límite de intentos de registro de personal.

| Campo | Tipo | Nulo | Clave | Restricción | Descripción |
|---|---|---|---|---|---|
| clave | VARCHAR(64) | No | PK | | Clave que identifica el origen del intento (el README indica límite por IP) |
| intentos | INTEGER | No | | CHECK entre 1 y 6 | Intentos hechos en la ventana |
| ventana_inicia | TIMESTAMP WITH TIME ZONE | No | | | Inicio de la ventana de conteo |

Según el README del backend, el registro limita a cinco intentos por IP cada 15 minutos.

---

## Origen de los datos de ejemplo

- Catálogos, perfiles, permisos y opciones se cargan con las migraciones V2 y V3 (siempre).
- Con el perfil `demo` se cargan 20 ciudadanos ficticios, 19 antecedentes y 10 integrantes simulados de personal. El ciudadano `9000000020` **no tiene** antecedente a propósito: sirve para probar la respuesta «sin información».
- Los estados REQUERIDO / NO_REQUERIDO son **datos de prueba**. No existe una fuente oficial, y la columna `antecedente.fuente` lo declara (`SIMULADA`).
