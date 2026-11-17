# Pruebas — Web administrativa

## 1. Pruebas automáticas (`./mvnw test`)

No necesitan backend: usan el perfil `mock`, dobles de Mockito o un servidor HTTP simulado (`MockRestServiceServer`).

| Clase | Pruebas | Qué cubre |
|---|---:|---|
| `AccesoYEstadosTest` | 21 | Sin sesión → login; rol incorrecto; 401/403 en pantallas; estados vacío y error; errores del backend al registrar/editar; reporte con API caída o sin permisos |
| `ClientesRestTest` | 19 | Clientes REST: URL, token Bearer, JSON del contrato OpenAPI (fechas ISO, PUT sin identificación), traducción de 400/401/403/404/409/5xx/red, PDF del reporte |
| `VistasTest` | 12 | Cada pantalla responde y renderiza su vista |
| `RegistroCiudadanoTest` | 10 | Campos obligatorios, formatos, fechas, estatura, catálogos, duplicado y confirmación |
| `ConsultaYEdicionTest` | 9 | Consulta por identificación, detalle, edición con confirmación, ruta inválida |
| `ReportesTest` | 6 | Filtro, PDF adjunto/en línea sin caché, sin resultados, estado inválido, sin sesión; estructura del PDF simulado |
| `DefectosCorregidosTest` | 6 | Regresión de los defectos D7 a D10 |
| `UsuariosYPerfilesTest` | 23 | Usuarios y perfiles (CA31 a CA38): acceso sin sesión, listado y filtros, crear, validar, duplicados, editar, no autodesactivarse, último administrador, opciones y permisos por perfil |
| `SeguridadRestTest` | 10 | Clientes REST de usuarios y perfiles: URL, filtros, cuerpos sin contraseña ni identificación, códigos 409, listas de perfiles y permisos |
| `SeguridadEstadosTest` | 9 | Usuarios y perfiles ante 401, 403, 400, 404 y API o correo caídos |
| **Total** | **125** | |

Qué criterio de aceptación verifica cada prueba: [criterios-de-aceptacion.md](criterios-de-aceptacion.md).

## 2. Casos de prueba de la interfaz contra el backend real

Ejecutados el 2026-11-10 con el backend de `develop` (commit `8089c87`, perfiles `dev,demo`) sobre PostgreSQL,
con la web en perfil `api`. Para el caso de rol incorrecto se creó en la base de prueba un usuario con perfil
`PERSONAL_FUERZA_PUBLICA`. Resultado: **37 de 37 OK**.

### Permisos del administrador

| Id | Caso | Resultado esperado | Resultado |
|---|---|---|---|
| P01 | Abrir `/ciudadanos` sin sesión | Redirige al login | OK |
| P02 | Pedir `/reportes/pdf` sin sesión | Redirige al login | OK |
| P03 | Login sin datos | «Ingrese identificación y contraseña.» | OK |
| P04 | Contraseña incorrecta | «Identificación o contraseña incorrectas.» (no revela si el usuario existe) | OK |
| P05 | Login de un usuario de fuerza pública | «Su usuario no tiene permisos de administrador.» | OK |
| P06 | Después de P05, abrir `/ciudadanos` | No quedó sesión: redirige al login | OK |
| P07 | Login del administrador | Entra al panel y muestra «Sesión: …» | OK |

### Campos obligatorios y formatos (registro)

| Id | Caso | Resultado esperado | Resultado |
|---|---|---|---|
| F01 | Enviar el formulario vacío | Los 10 campos marcados en rojo | OK |
| F02 | Identificación `12ab56` | «Debe tener entre 6 y 10 dígitos…» | OK |
| F03 | Identificación de 5 dígitos | «Debe tener entre 6 y 10 dígitos…» | OK |
| F04 | Nombres con números | «Solo letras y espacios.» | OK |
| F05 | Fecha de nacimiento futura | «No puede ser una fecha futura.» | OK |
| F06 | Fecha con texto | «Fecha inválida» | OK |
| F07 | Expedición anterior al nacimiento | «Debe ser posterior a la fecha de nacimiento.» | OK |
| F08 | Estatura 3.10 | «Debe estar entre 0.50 y 2.50 m…» | OK |
| F09 | Estatura 1.705 | «…con máximo 2 decimales» | OK |
| F10 | RH fuera del catálogo | «Seleccione + o -.» | OK |
| F11 | Error en un campo | Los demás datos escritos se conservan | OK |

### Registro, duplicado, consulta y edición

| Id | Caso | Resultado esperado | Resultado |
|---|---|---|---|
| R01 | Registro válido | Confirmación y fila resaltada | OK |
| R02 | Verificar en la base | El ciudadano existe | OK |
| R03 | Registrar la misma identificación | «Ya existe un ciudadano con esta identificación.» | OK |
| R04 | Consultar `9.000.000.001` | Abre el detalle de `9000000001` | OK |
| R05 | Edición válida | «Cambios guardados para …» en el detalle | OK |
| R06 | Verificar en la base | El cambio quedó guardado | OK |
| R07 | Alterar la identificación al editar | «La identificación de un ciudadano no se puede modificar.» | OK |

### Reportes

| Id | Caso | Resultado esperado | Resultado |
|---|---|---|---|
| T01 | Reporte «Requerido» | Archivo PDF (`%PDF-`) | OK |
| T02 | Estado inválido | Pide elegir un estado | OK |
| — | Reporte sin resultados (todos los antecedentes en un solo estado) | «No hay ciudadanos con estado «No requerido». No se generó el reporte.» | OK (verificado aparte; datos restaurados) |

### Seguridad y navegación

| Id | Caso | Resultado esperado | Resultado |
|---|---|---|---|
| S01 | Cabeceras de una pantalla con datos | `Cache-Control: no-store` | OK |
| S02 | Cabeceras del panel | `X-Frame-Options: DENY` | OK |
| S03 | `/ciudadanos?pagina=99` | «La página solicitada no existe.» | OK |
| S04 | `/ciudadanos/abc` | Página «Página no encontrada» (404) | OK |

### Errores de red y de sesión

| Id | Caso | Resultado esperado | Resultado |
|---|---|---|---|
| N01 | Cerrar sesión | Vuelve al login con «Sesión cerrada.» | OK |
| N02 | Reusar la cookie de la sesión cerrada | Redirige al login | OK |
| N03 | Backend apagado: listado | «No fue posible conectar…» con **Reintentar** | OK |
| N04 | Backend apagado: registrar | Mensaje de error y los datos escritos se conservan | OK |
| N05 | Backend apagado: reporte | «…No se generó el reporte.» | OK |
| N06 | Backend apagado: login | «No fue posible conectar…» sin entrar | OK |
| — | Token rechazado por el backend (401) | Vuelve al login con «Su sesión expiró» | OK (verificado reiniciando el backend con otro secreto JWT) |

Los casos que no escriben datos, y opcionalmente los de registro y edición, están en `scripts/prueba-humo.sh` para
repetirlos en el entorno de integración. Con los casos de usuarios y perfiles (U01 a U07), la ejecución del
2026-11-16 dio 33 de 33 OK contra el backend real y 31 de 31 en modo `mock` (allí se omiten los dos de rol incorrecto).

## 3. Validación en el navegador (JavaScript)

MockMvc no ejecuta JavaScript. `static/js/validacion.js` se probó con una página que simula al usuario
(campos vacíos, identificación `12ab`, nombres con solo espacios, tildes, apellidos con números, expedición anterior
al nacimiento y corrección posterior, fecha futura, estatura `1.705` y `3`, envío válido). Los 12 resultados se
verificaron en el navegador y fueron correctos.

## 4. Defectos encontrados y corregidos

| Id | Defecto | Corrección |
|---|---|---|
| D1 | Al editar, una identificación alterada se mostraba en el formulario | Se rechaza la petición si no coincide con la URL |
| D2 | La primera redirección incluía `;jsessionid=…` en la URL | Sesión solo por cookie (`tracking-modes: cookie`) |
| D3 | Las fechas se enviaban a la API como `[1995,2,14]` | El cliente usa el `ObjectMapper` de Spring Boot (fechas ISO) |
| D4 | Un token vencido en la sesión hacía fallar un nuevo login (401) | Se limpia el token antes de iniciar sesión |
| D5 | Nombres vacíos mostraban dos errores a la vez | Patrón `[\p{L} ]*`; el vacío lo detecta «obligatorio» |
| D6 | Una respuesta JSON cuando se esperaba el PDF daba un error genérico | El conversor JSON ya no lee respuestas `byte[]` |
| D7 | `?pagina=99` decía «Aún no hay ciudadanos registrados» | Mensaje «La página solicitada no existe» |
| D8 | Tras cerrar sesión, «Atrás» mostraba datos desde la caché | `Cache-Control: no-store` en las pantallas |
| D9 | El panel se podía incrustar en otro sitio (clickjacking) | `X-Frame-Options: DENY` y CSP `frame-ancestors 'none'` |
| D10 | La sesión web vencía a los 30 min aunque «Recordar» diera 8 h | La sesión dura lo mismo que el token |
| D11 | README con una tabla rota y estado desactualizado | README reescrito |

## 5. Limitaciones conocidas

- La respuesta del login no incluye el rol; la web lo verifica con una consulta adicional. Si seguridad agrega el rol
  al login (o un endpoint tipo `/api/auth/yo`), esa consulta se puede quitar.
- El rechazo 400 del backend al registrar/editar solo se probó con dobles: la web valida las mismas reglas antes,
  así que esos datos no llegan a la API.
- **Usuarios y perfiles:** el backend no permite crear ni modificar perfiles (CA35), no expone las opciones del sistema
  (la web las deduce del código de cada permiso, CA36) y autoriza por perfil, no por los permisos asignados (CA38).
  Detalle en [criterios-de-aceptacion.md](criterios-de-aceptacion.md#pendientes-que-dependen-del-backend).
- El listado espera el formato de página actual de Spring Data (`content`, `totalElements`…). Si el backend cambia la
  serialización a `VIA_DTO`, hay que ajustar `PaginaCiudadanos`.

## 6. Evidencias

Ejecución del 2026-11-16: backend de `develop` (commit `a9ccb66`) con perfiles `demo,dev` sobre una base PostgreSQL
limpia, un receptor SMTP de prueba para el correo SIMULADO, y la web de `feature/web` en perfil `api`. Todos los datos son ficticios. Para el rol incorrecto se creó en
esa base un usuario con perfil `PERSONAL_FUERZA_PUBLICA`.

| Evidencia | Resultado |
|---|---|
| [evidencias/mvn-test-salida.txt](evidencias/mvn-test-salida.txt) | `./mvnw test`: **125 de 125** pruebas automáticas (con dobles, sin backend) |
| [evidencias/prueba-humo-salida.txt](evidencias/prueba-humo-salida.txt) | `scripts/prueba-humo.sh` contra el backend real, con rol incorrecto y `ESCRITURA=1`: **33 de 33 OK** |

Capturas tomadas en Firefox contra el backend real. La columna «Casos» remite a los identificadores de la sección 2.

| Id | Qué se hizo | Resultado observado | Casos | Captura |
|---|---|---|---|---|
| W-01 | Abrir `/ciudadanos` sin sesión | Redirige al login | P01 | [captura](evidencias/W-01-sin-sesion-redirige-al-login.png) |
| W-02 | Login con contraseña incorrecta | «Identificación o contraseña incorrectas.» | P04 | [captura](evidencias/W-02-login-credenciales-incorrectas.png) |
| W-03 | Login de un usuario de fuerza pública | «Su usuario no tiene permisos de administrador.» | P05 | [captura](evidencias/W-03-login-rol-sin-permisos.png) |
| W-04 | Login del administrador | Panel principal con «Sesión: …» y el total de ciudadanos | P07 | [captura](evidencias/W-04-panel-principal.png) |
| W-05 | Abrir el listado | Tabla paginada, «Mostrando 1–20 de 22» | — | [captura](evidencias/W-05-listado.png) |
| W-06 | Buscar `90000000` | Solo los ciudadanos que coinciden | — | [captura](evidencias/W-06-listado-filtrado.png) |
| W-07 | Buscar algo que no existe | «No hay resultados para …» | — | [captura](evidencias/W-07-busqueda-sin-resultados.png) |
| W-08 | Consultar `9.000.000.001` | Abre el detalle de `9000000001` | R04 | [captura](evidencias/W-08-consulta-detalle.png) |
| W-09 | Consultar `12ab` | «Identificación inválida…» | — | [captura](evidencias/W-09-consulta-identificacion-invalida.png) |
| W-10 | Enviar el registro vacío | Los 10 campos marcados (validación en el navegador) | F01 | [captura](evidencias/W-10-registro-campos-obligatorios.png) |
| W-11 | Identificación `12ab56`, nombres con números, expedición anterior al nacimiento, estatura 3.10 | Un mensaje por campo; lo demás se conserva | F02, F04, F07, F08, F11 | [captura](evidencias/W-11-registro-formatos-invalidos.png) |
| W-12 | Registrar la identificación `9000000001`, que ya existe | «Ya existe un ciudadano con esta identificación.» (409 del backend) | R03 | [captura](evidencias/W-12-registro-identificacion-duplicada.png) |
| W-13 | Registro válido | Confirmación y fila resaltada | R01 | [a](evidencias/W-13a-registro-diligenciado.png), [b](evidencias/W-13b-registro-confirmado.png) |
| W-14 | Editar el lugar de nacimiento | Formulario precargado con la identificación bloqueada; «Cambios guardados para …» | R05 | [a](evidencias/W-14a-edicion-precargada.png), [b](evidencias/W-14b-edicion-guardada.png) |
| W-15 | Abrir Reportes | Filtro Requerido / No requerido, Ver PDF y Descargar PDF | — | [captura](evidencias/W-15-reportes-filtro.png) |
| W-16 | Pedir el reporte con un estado inválido | «Seleccione un estado: Requerido o No requerido.» | T02 | [captura](evidencias/W-16-reportes-estado-invalido.png) |
| W-17 | Reporte «No requerido» con todos los antecedentes en «Requerido» | «No hay ciudadanos con estado «No requerido». No se generó el reporte.» (datos restaurados después) | Sin resultados | [captura](evidencias/W-17-reportes-sin-resultados.png) |
| W-18 | Abrir `/ciudadanos?pagina=99` | «La página solicitada no existe.» | S03 | [captura](evidencias/W-18-pagina-inexistente.png) |
| W-19 | Abrir `/ciudadanos/abc` | Página «Página no encontrada» (404) | S04 | [captura](evidencias/W-19-ruta-invalida-404.png) |
| W-20 | Backend apagado: abrir el listado | «No fue posible conectar…» con **Reintentar** | N03 | [captura](evidencias/W-20-backend-apagado-listado.png) |
| W-21 | Backend apagado: registrar | Mensaje de error y los datos escritos se conservan | N04 | [captura](evidencias/W-21-backend-apagado-registro-conserva-datos.png) |
| W-22 | Backend apagado: pedir el reporte | «…No se generó el reporte.» | N05 | [captura](evidencias/W-22-backend-apagado-reporte.png) |
| W-23 | Cerrar sesión | Vuelve al login con «Sesión cerrada.» | N01 | [captura](evidencias/W-23-sesion-cerrada.png) |
| W-24 | Backend apagado: login | «No fue posible conectar…» sin entrar | N06 | [captura](evidencias/W-24-backend-apagado-login.png) |

### Usuarios y perfiles

Los identificadores W-25 a W-38 son los casos de prueba de estas pantallas; la columna «Criterios» remite a
[criterios-de-aceptacion.md](criterios-de-aceptacion.md).

| Id | Qué se hizo | Resultado observado | Criterios | Captura |
|---|---|---|---|---|
| W-25 | Abrir el panel | Menú y tarjeta «Usuarios y perfiles» | — | [captura](evidencias/W-25-panel-con-usuarios-y-perfiles.png) |
| W-26 | Abrir Usuarios | Listado con perfil y estado de cada usuario | CA31 | [captura](evidencias/W-26-usuarios-listado.png) |
| W-27 | Filtrar administradores inactivos | «No hay usuarios con ese filtro.» | CA31 | [captura](evidencias/W-27-usuarios-filtro-sin-resultados.png) |
| W-28 | Enviar «Crear usuario» vacío | Los 5 campos marcados | CA34 | [captura](evidencias/W-28-usuario-campos-obligatorios.png) |
| W-29 | Identificación `12ab` y correo sin arroba | Un mensaje por campo; lo demás se conserva | CA34 | [captura](evidencias/W-29-usuario-formatos-invalidos.png) |
| W-30 | Crear un usuario válido | «Usuario creado… La contraseña temporal se envió a su correo»; el receptor SMTP recibió «[SIMULADO] Credenciales de acceso» | CA32 | [a](evidencias/W-30a-usuario-diligenciado.png), [b](evidencias/W-30b-usuario-creado.png) |
| W-31 | Crear otro con la misma identificación | «Ya existe un usuario con esta identificación.» (409) | CA34 | [captura](evidencias/W-31-usuario-identificacion-duplicada.png) |
| W-32 | Crear otro con el mismo correo | «Este correo ya está registrado por otro usuario.» (409) | CA34 | [captura](evidencias/W-32-usuario-correo-duplicado.png) |
| W-33 | Editar el usuario: cambiar nombres y pasar a Inactivo | Formulario precargado con la identificación bloqueada; «Cambios guardados para …» | CA33 | [a](evidencias/W-33a-usuario-edicion-precargada.png), [b](evidencias/W-33b-usuario-actualizado-inactivo.png) |
| W-34 | Filtrar por estado Inactivo | Solo el usuario desactivado | CA31 | [captura](evidencias/W-34-usuarios-filtro-inactivos.png) |
| W-35 | El administrador en sesión intenta pasar a Inactivo | «No puede desactivar su propia cuenta ni quitarse el perfil de administrador.» (409) | CA34 | [captura](evidencias/W-35-usuario-no-puede-desactivarse.png) |
| W-36 | Abrir Perfiles y permisos | Las cuatro opciones del sistema con sus permisos; el de seguridad, marcado y bloqueado | CA36 | [captura](evidencias/W-36-perfiles-administrador.png) |
| W-37 | Dar «Generar reportes» al perfil de fuerza pública y guardar | «Permisos guardados. El perfil quedó con 2 permisos.» (después se dejó como estaba) | CA37 | [a](evidencias/W-37a-perfil-fuerza-publica.png), [b](evidencias/W-37b-permisos-guardados.png) |
| W-38 | Petición alterada que le quita la gestión de seguridad al perfil Administrador | «El perfil Administrador debe conservar el permiso…» (409); nada cambia | CA37 | [captura](evidencias/W-38-perfil-administrador-conserva-seguridad.png) |

Sin captura: «debe quedar al menos un administrador activo» (lo verifica
`UsuariosYPerfilesTest.noSePuedeDejarElSistemaSinAdministradorActivo`; contra el backend real haría falta la
contraseña de un segundo administrador, que solo llega por correo).

Sin captura en la ejecución de ciudadanos y reportes: el PDF del reporte «Requerido» (lo verifica el caso C03 del script de humo, que
comprueba que la respuesta empieza por `%PDF-`; los PDF no se versionan) y el token rechazado con 401.
