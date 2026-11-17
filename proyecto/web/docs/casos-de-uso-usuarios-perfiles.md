# Casos de uso — Usuarios y perfiles (MVP 2)

> **Estado: implementado en la web** (pantallas `/usuarios` y `/perfiles`) sobre la API ya existente
> (`/api/usuarios`, `/api/perfiles` y `/api/permisos`). Los cuatro puntos que estaban abiertos quedaron resueltos
> en [Decisiones](#decisiones); se tomaron a partir del comportamiento real del backend y de los criterios de
> aceptación, y **falta el visto bueno de Julián (seguridad)**.

Especificación de los casos CU-W08 y CU-W09 del actor **Administrador**: historias HU08 (usuarios, criterios CA31 a
CA34) y HU09 (perfiles, opciones y permisos, criterios CA35 a CA38), requisito RF06. El diagrama general y las
condiciones comunes (sin sesión, 401, 403 y API caída) están en [casos-de-uso.md](casos-de-uso.md); el cumplimiento
criterio por criterio, en [criterios-de-aceptacion.md](criterios-de-aceptacion.md).

## Diagrama

```mermaid
flowchart LR
    admin(["👤 Administrador"])
    correo(["✉ Servicio de correo<br/>(SIMULADO)"])

    subgraph sistema["Web administrativa — Usuarios y perfiles"]
        cu08a(["CU-W08.1<br/>Listar y filtrar usuarios"])
        cu08b(["CU-W08.2<br/>Crear usuario"])
        cu08c(["CU-W08.3<br/>Actualizar usuario<br/>(datos, perfil y estado)"])
        cu09a(["CU-W09.1<br/>Consultar perfiles<br/>y sus permisos"])
        cu09b(["CU-W09.2<br/>Reemplazar permisos<br/>de un perfil"])
        pwd(["Generar y enviar<br/>contraseña temporal"])
    end

    admin --- cu08a
    admin --- cu08b
    admin --- cu08c
    admin --- cu09a
    admin --- cu09b
    cu08b -. "«include»" .-> pwd
    pwd --- correo
    cu08c -. "«extend»<br/>Editar" .-> cu08a
    cu09b -. "«extend»" .-> cu09a
```

## Resumen

| Id | Caso de uso | Historia | Criterios | API | Pantalla web |
|---|---|---|---|---|---|
| CU-W08.1 | Listar y filtrar usuarios | HU08 | CA31 | `GET /api/usuarios?estado=&perfil=&page=&size=` | `/usuarios` |
| CU-W08.2 | Crear usuario | HU08 | CA32, CA34 | `POST /api/usuarios` | `/usuarios/nuevo` |
| CU-W08.3 | Actualizar usuario | HU08 | CA33, CA34 | `GET` y `PUT /api/usuarios/{identificacion}` | `/usuarios/{identificacion}/editar` |
| CU-W09.1 | Consultar perfiles, opciones y permisos | HU09 | CA36 | `GET /api/perfiles`, `GET /api/permisos`, `GET /api/perfiles/{id}/permisos` | `/perfiles` |
| CU-W09.2 | Asignar permisos a un perfil | HU09 | CA37, CA38 | `PUT /api/perfiles/{id}/permisos` | `/perfiles` |

CA35 (crear o modificar un perfil) no tiene caso de uso: el backend no lo ofrece (ver [Decisiones](#decisiones)).

Todas las rutas exigen el perfil ADMINISTRADOR en el backend (ver
[matriz de rol y permiso](../../backend/docs/decisiones/matriz-rol-permiso.md)).

## Reglas de negocio

| Id | Regla | Respuesta de la API |
|---|---|---|
| RN1 | No se eliminan usuarios; se desactivan (estado INACTIVO) | No existe `DELETE` |
| RN2 | La identificación y el correo son únicos | 409 `DUPLICADO` |
| RN3 | La contraseña temporal la genera el sistema y se envía por correo (SIMULADO); nunca se muestra ni se devuelve | — |
| RN4 | Un administrador no puede desactivarse ni quitarse su propio perfil | 409 `AUTODESACTIVACION_NO_PERMITIDA` |
| RN5 | Debe quedar al menos un administrador activo | 409 `ULTIMO_ADMIN_ACTIVO` |
| RN6 | El catálogo de perfiles es fijo: ADMINISTRADOR y PERSONAL_FUERZA_PUBLICA. No se crean ni se borran perfiles | — |
| RN7 | El perfil ADMINISTRADOR debe conservar el permiso «Administrar usuarios y perfiles» (`SEGURIDAD_ADMINISTRAR`) | 409 `PERMISOS_ADMIN_REQUERIDOS` |
| RN8 | Las respuestas nunca incluyen contraseñas ni sus hashes | — |

## CU-W08.1 — Listar y filtrar usuarios

| | |
|---|---|
| **Actor principal** | Administrador |
| **Propósito** | Ver quién tiene acceso al sistema, con qué perfil y en qué estado. |
| **Precondiciones** | Sesión iniciada como administrador. |
| **Postcondición de éxito** | La web muestra los usuarios que cumplen el filtro. |
| **Disparador** | El administrador abre **Usuarios y perfiles**. |

**Flujo principal**

1. La web pide la primera página de usuarios a la API y muestra identificación, nombres, apellidos, correo, perfil y estado.
2. El administrador filtra por perfil, por estado o por ambos.
3. La web muestra los usuarios que coinciden, de a 20 por página, ordenados por identificación.

**Flujos alternos**

- **3a. Ningún usuario cumple el filtro.** La web muestra «No hay usuarios con ese filtro.» y permite limpiarlo.
- **2a. El filtro llega con un valor que no existe** (URL alterada). La web lo ignora y muestra todos los usuarios.

## CU-W08.2 — Crear usuario

| | |
|---|---|
| **Actor principal** | Administrador |
| **Actor secundario** | Servicio de correo (SIMULADO) |
| **Propósito** | Dar acceso al sistema a una persona, asignándole un perfil. |
| **Precondiciones** | Sesión iniciada como administrador. |
| **Postcondición de éxito** | El usuario queda creado en estado ACTIVO y recibe su contraseña temporal por correo. |
| **Disparador** | El administrador pulsa **Crear usuario**. |

**Flujo principal**

1. La web muestra el formulario: identificación, nombres, apellidos, correo y perfil.
2. El administrador diligencia los campos y envía el formulario.
3. La web valida: identificación de 6 a 10 dígitos, nombres y apellidos de máximo 100 caracteres, correo con formato
   válido y máximo 200 caracteres, y perfil elegido.
4. La web envía los datos a la API (`POST /api/usuarios`).
5. La API crea el usuario, genera una contraseña temporal, guarda solo su hash y la envía al correo indicado.
6. La web confirma la creación e indica que la contraseña llega por correo (RN3).

**Flujos alternos**

- **3a. Hay campos vacíos o inválidos.** La web marca cada campo con su mensaje y conserva lo escrito.
- **5a. La identificación ya está registrada** (RN2). La web muestra «Ya existe un usuario con esta identificación.»
  en ese campo.
- **5b. El correo ya está registrado** (RN2). La web muestra «Este correo ya está registrado por otro usuario.» en ese campo.
- **5c. El perfil no existe** (404). La web muestra «El perfil seleccionado ya no existe.» en el campo perfil.
- **5d. El servicio de correo o la API no están disponibles** (503 o red). La web muestra «No fue posible conectar
  con el servidor… El usuario no se creó.» y conserva lo escrito.

## CU-W08.3 — Actualizar usuario

| | |
|---|---|
| **Actor principal** | Administrador |
| **Propósito** | Corregir los datos de un usuario, cambiarle el perfil, o desactivarlo y reactivarlo. |
| **Precondiciones** | Sesión iniciada como administrador. El usuario existe. |
| **Postcondición de éxito** | Los cambios quedan guardados. Si cambió el perfil o el estado, los tokens anteriores del usuario dejan de servir. |
| **Disparador** | El administrador pulsa **Editar** sobre un usuario del listado. |

**Flujo principal**

1. La web pide el usuario a la API y muestra el formulario precargado, con la identificación bloqueada.
2. El administrador cambia nombres, apellidos, correo, perfil o estado (ACTIVO / INACTIVO) y guarda.
3. La web valida los campos como en CU-W08.2.
4. La web envía los cambios a la API (`PUT /api/usuarios/{identificacion}`).
5. La web vuelve al listado con «Cambios guardados para …» y la fila resaltada.

**Flujos alternos**

- **4a. El correo ya pertenece a otro usuario** (RN2). La web lo muestra en el campo correo.
- **4b. El administrador intenta desactivarse o quitarse su propio perfil** (RN4). La web muestra «No puede
  desactivar su propia cuenta ni quitarse el perfil de administrador.» y no guarda.
- **4c. El cambio dejaría el sistema sin administradores activos** (RN5). La web muestra «Debe quedar al menos un
  administrador activo en el sistema.» y no guarda.
- **2a. La identificación enviada no coincide con la del usuario que se edita.** La web rechaza la petición con
  «La identificación de un usuario no se puede modificar.» y no llama a la API.
- **1a. El usuario ya no existe** (404). La web muestra «No se encontró el usuario con identificación …» en el listado.

## CU-W09.1 — Consultar perfiles, opciones y permisos

| | |
|---|---|
| **Actor principal** | Administrador |
| **Propósito** | Saber qué puede hacer cada perfil en cada opción del sistema. |
| **Precondiciones** | Sesión iniciada como administrador. |
| **Postcondición de éxito** | La web muestra los permisos del perfil elegido. |
| **Disparador** | El administrador abre la pestaña **Perfiles y permisos**. |

**Flujo principal**

1. La web pide a la API los perfiles y el catálogo completo de permisos.
2. El administrador elige un perfil.
3. La web pide los permisos de ese perfil y los muestra marcados sobre el catálogo completo, agrupados por opción
   del sistema.

| Opción del sistema | Permisos |
|---|---|
| Gestión de ciudadanos | Consultar ciudadanos, registrar ciudadanos, actualizar ciudadanos |
| Consulta de antecedentes | Consultar antecedentes, actualizar estados de demostración |
| Reportes de ciudadanos | Generar reportes |
| Gestión de seguridad | Administrar usuarios y perfiles |

## CU-W09.2 — Asignar permisos a un perfil

| | |
|---|---|
| **Actor principal** | Administrador |
| **Propósito** | Ajustar lo que un perfil puede hacer. |
| **Precondiciones** | Sesión iniciada como administrador. Se está consultando un perfil (CU-W09.1). |
| **Postcondición de éxito** | El perfil queda exactamente con los permisos marcados. |
| **Disparador** | El administrador marca o desmarca permisos y pulsa **Guardar permisos**. |

**Flujo principal**

1. El administrador marca los permisos que debe tener el perfil.
2. La web envía la lista completa a la API (`PUT /api/perfiles/{id}/permisos`); la lista reemplaza a la anterior.
3. La API guarda los permisos.
4. La web confirma «Permisos guardados. El perfil quedó con N permisos.» y muestra la asignación actualizada.

**Flujos alternos**

- **2a. Se intenta quitar «Administrar usuarios y perfiles» al perfil ADMINISTRADOR** (RN7). La web muestra ese
  permiso marcado y bloqueado; si aun así llega la petición, la API responde 409 y la web muestra «El perfil
  Administrador debe conservar el permiso «Administrar usuarios y perfiles». No se guardaron los cambios.»
- **3a. El perfil o algún permiso ya no existe** (404). La web recarga el catálogo y pide repetir la selección.
- **3b. La API no responde.** La web muestra «…Los permisos no se guardaron.»

Un cambio de permisos solo tiene efecto real cuando el backend lo aplica: la web nunca decide por su cuenta qué
puede hacer un usuario (CA38). Hoy el backend autoriza cada ruta por perfil, según la
[matriz de rol y permiso](../../backend/docs/decisiones/matriz-rol-permiso.md).

## Decisiones

Los cuatro puntos que estaban abiertos, cómo quedaron y por qué.

| # | Punto | Decisión | Motivo |
|---|---|---|---|
| 1 | ¿La web muestra las opciones del sistema o solo los permisos? | **Muestra las opciones**, cada una con sus permisos | CA36 pide consultar las opciones y CA37 asignar permisos «sobre determinadas opciones». El backend no expone las opciones por la API, pero el código de cada permiso empieza por el de su opción (`CIUDADANOS_LEER` → `CIUDADANOS`), que coincide con la tabla `opcion`; la web agrupa con esa regla |
| 2 | ¿Al crear un usuario de fuerza pública desde la web se piden fuerza, rango e ID Fuerza? | **No.** La web pide identificación, nombres, apellidos, correo y perfil | `POST /api/usuarios` no recibe esos datos. El registro del personal con fuerza y rango, validado contra Defensa, es de la app móvil (HU01 y HU02) |
| 3 | Texto de los mensajes para los errores 409 | Definidos en la tabla de abajo | Mensajes en español, sin códigos técnicos, que dicen qué hacer |
| 4 | Historia y criterios de cada caso | HU08 → CU-W08 (CA31 a CA34); HU09 → CU-W09 (CA35 a CA38) | Tabla de criterios de aceptación del proyecto |

**Mensajes de la web para cada respuesta 409 de la API**

| Código de la API | Dónde se muestra | Mensaje |
|---|---|---|
| `DUPLICADO` (identificación) | Campo identificación | Ya existe un usuario con esta identificación. |
| `DUPLICADO` (correo) | Campo correo | Este correo ya está registrado por otro usuario. |
| `AUTODESACTIVACION_NO_PERMITIDA` | Encabezado del formulario | No puede desactivar su propia cuenta ni quitarse el perfil de administrador. |
| `ULTIMO_ADMIN_ACTIVO` | Encabezado del formulario | Debe quedar al menos un administrador activo en el sistema. |
| `PERMISOS_ADMIN_REQUERIDOS` | Encabezado de perfiles | El perfil Administrador debe conservar el permiso «Administrar usuarios y perfiles». No se guardaron los cambios. |

**Lo que no se puede cumplir desde la web**

- **CA35 — crear o modificar un perfil.** El backend tiene un catálogo fijo de dos perfiles y no ofrece endpoints
  para crearlos ni editarlos (RN6). La web solo puede cambiar los permisos de un perfil existente. Cumplir CA35
  completo requiere que el backend agregue `POST` y `PUT /api/perfiles`.
- **CA36 — opciones.** La web las muestra, pero deducidas del código de cada permiso. Si el backend publica
  `GET /api/opciones`, la web debe leerlas de ahí.
