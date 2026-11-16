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
| **Total** | **83** | |

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
repetirlos en el entorno de integración (26 de 26 OK contra el backend real; 19 de 19 en modo `mock`).

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
- El listado espera el formato de página actual de Spring Data (`content`, `totalElements`…). Si el backend cambia la
  serialización a `VIA_DTO`, hay que ajustar `PaginaCiudadanos`.
