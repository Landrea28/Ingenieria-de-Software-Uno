# Criterios de aceptación — Web administrativa

Trazabilidad de los criterios de aceptación que le corresponden a la web del **Administrador**: dónde se cumple cada
uno, qué prueba automática lo verifica y qué evidencia hay contra el backend real. Los criterios del personal de la
fuerza pública (CA01 a CA09 y CA26 a CA30) son de la app móvil y están en
[su documentación](../../mobile/docs/casos-de-uso.md).

**Estado:** *Cumple* · *Parcial* (se cumple una parte o con una salvedad) · *No cumple*. Las capturas `W-xx` están en
[evidencias/](evidencias/) y se describen en [pruebas.md](pruebas.md#6-evidencias).

## Resumen

| Historia | Criterios | Cumple | Parcial | No cumple |
|---|---|---:|---:|---:|
| HU03 — Inicio de sesión (administrador) | CA10 a CA14 | 5 | 0 | 0 |
| HU04 — Registrar ciudadano | CA15 a CA18 | 4 | 0 | 0 |
| HU05 — Consultar ciudadanos | CA19 a CA21 | 3 | 0 | 0 |
| HU06 — Actualizar ciudadano | CA22 a CA25 | 4 | 0 | 0 |
| HU08 — Usuarios | CA31 a CA34 | 4 | 0 | 0 |
| HU09 — Perfiles, opciones y permisos | CA35 a CA38 | 1 | 2 | 1 |
| HU10 — Reporte PDF | CA39 a CA43 | 4 | 1 | 0 |
| Transversal | CA44 | 1 | 0 | 0 |
| **Total** | **30** | **26** | **3** | **1** |

Los cuatro que no se cumplen del todo (CA35, CA36, CA38 y CA43) dependen del backend; se explican en
[Pendientes](#pendientes-que-dependen-del-backend).

## HU03 — Inicio de sesión del administrador

HU03 está escrita para el personal de la fuerza pública. El administrador usa el mismo inicio de sesión de la API
desde la web, por eso se verifican los mismos criterios.

| Id | Criterio | Estado | Cómo se cumple en la web | Prueba automática | Evidencia |
|---|---|---|---|---|---|
| CA10 | Ingreso de credenciales | Cumple | Formulario con identificación y contraseña; sin ambos datos no se consulta la API | `AccesoYEstadosTest.loginSinDatosMuestraMensaje` | W-01 |
| CA11 | Validación de credenciales | Cumple | La web envía las credenciales a `POST /api/auth/login`; valida el backend | `ClientesRestTest` (login) | Humo P06 |
| CA12 | Acceso exitoso | Cumple | Entra al panel y muestra «Sesión: …» | `AccesoYEstadosTest.loginExitosoEntraAlPanelYMuestraElUsuario` | W-04 |
| CA13 | Credenciales incorrectas | Cumple | «Identificación o contraseña incorrectas.», sin entrar y sin decir cuál falló | `AccesoYEstadosTest` (login) | W-02 |
| CA14 | Recordar contraseña | Cumple | Con «Recordar contraseña» la sesión dura lo mismo que el token (8 h en vez de 15 min). No se guarda la contraseña | `DefectosCorregidosTest` (defecto D10) | — |

Además, un usuario que no es administrador no puede entrar aunque sus credenciales sean correctas (W-03).

## HU04 — Registrar ciudadano

| Id | Criterio | Estado | Cómo se cumple en la web | Prueba automática | Evidencia |
|---|---|---|---|---|---|
| CA15 | Registro de datos del ciudadano | Cumple | Formulario con los 10 campos: nombres, apellidos, identificación, fecha y lugar de nacimiento, fecha y lugar de expedición, RH, grupo sanguíneo y estatura | `RegistroCiudadanoTest.registroValidoConfirmaYApareceEnElListado` | W-13a |
| CA16 | Campos obligatorios | Cumple | Sin los 10 campos no se guarda; cada campo pendiente queda marcado con su mensaje (navegador y servidor) | `RegistroCiudadanoTest.formularioVacioMarcaLosDiezCamposObligatorios` | W-10 |
| CA17 | Identificación única | Cumple | «Ya existe un ciudadano con esta identificación.» (409 del backend) | `RegistroCiudadanoTest.identificacionDuplicadaMuestraErrorEnElCampo` | W-12 |
| CA18 | Almacenamiento correcto | Cumple | Lo guardado se consulta de nuevo desde la API y coincide (confirmación, listado y detalle) | `ClientesRestTest.registrarEnviaLosDiezCamposSinIdYDevuelveElCreado` | W-13b, humo R01 y R03 |

## HU05 — Consultar ciudadanos

| Id | Criterio | Estado | Cómo se cumple en la web | Prueba automática | Evidencia |
|---|---|---|---|---|---|
| CA19 | Consulta de ciudadanos registrados | Cumple | Listado paginado con el total | `VistasTest.listadoMuestraCiudadanosMock` | W-05 |
| CA20 | Detalle del ciudadano | Cumple | «Ver» abre el detalle con los 10 datos | `ConsultaYEdicionTest.detalleMuestraLosDiezCampos` | W-08 |
| CA21 | Búsqueda por identificación | Cumple | «Consultar por identificación» abre el ciudadano; el filtro del listado también busca por identificación | `ConsultaYEdicionTest.consultaPorIdentificacionVaAlDetalleAunqueTengaPuntosOEspacios` | W-06, W-08 |

## HU06 — Actualizar ciudadano

| Id | Criterio | Estado | Cómo se cumple en la web | Prueba automática | Evidencia |
|---|---|---|---|---|---|
| CA22 | Selección para edición | Cumple | «Editar» abre el formulario con la información actual | `VistasTest.formularioDeEdicionCargaElCiudadano` | W-14a |
| CA23 | Validación de datos modificados | Cumple | Mismas reglas del registro, antes de llamar a la API | `ConsultaYEdicionTest.edicionInvalidaNoGuardaYMuestraLosErrores` | W-11 (mismo formulario) |
| CA24 | Guardado de cambios | Cumple | `PUT /api/ciudadanos/{identificacion}` | `ConsultaYEdicionTest.edicionValidaGuardaYConfirmaEnElDetalle` | W-14b, humo R04 |
| CA25 | Confirmación de actualización | Cumple | «Cambios guardados para …» en el detalle | La misma | W-14b |

## HU08 — Usuarios

| Id | Criterio | Estado | Cómo se cumple en la web | Prueba automática | Evidencia |
|---|---|---|---|---|---|
| CA31 | Consulta de usuarios | Cumple | Listado con perfil y estado, y filtros por ambos | `UsuariosYPerfilesTest.listadoMuestraLosUsuariosConPerfilYEstado` | W-26, W-34 |
| CA32 | Creación de usuarios | Cumple | Formulario con identificación, nombres, apellidos, correo y perfil. La contraseña la genera el backend y llega por correo | `UsuariosYPerfilesTest.crearUsuarioValidoConfirmaSinMostrarLaContrasena` | W-30a, W-30b |
| CA33 | Modificación de usuarios | Cumple | Edición de datos, perfil y estado | `UsuariosYPerfilesTest.actualizarCambiaDatosYEstado` | W-33a, W-33b |
| CA34 | Validación antes de guardar | Cumple | Campos obligatorios y formatos en navegador y servidor; duplicados y reglas del backend junto al campo | `UsuariosYPerfilesTest` (formulario vacío, formatos, duplicados) | W-28, W-29, W-31, W-32, W-35 |

## HU09 — Perfiles, opciones y permisos

| Id | Criterio | Estado | Cómo se cumple en la web | Prueba automática | Evidencia |
|---|---|---|---|---|---|
| CA35 | Gestión de perfiles (crear o modificar un perfil) | **No cumple** | La web solo puede cambiar los permisos de los dos perfiles existentes. El backend no permite crear ni editar perfiles | — | W-36 |
| CA36 | Consulta de opciones | **Parcial** | La web muestra las cuatro opciones del sistema con sus permisos, pero las deduce del código de cada permiso porque la API no las expone | `UsuariosYPerfilesTest.perfilesMuestraLasOpcionesDelSistemaConSusPermisos` | W-36 |
| CA37 | Asignación de permisos | Cumple | Marcar permisos por opción y guardar (`PUT /api/perfiles/{id}/permisos`); la asignación queda guardada | `UsuariosYPerfilesTest.guardarPermisosReemplazaLaAsignacionDelPerfil` | W-37a, W-37b, W-38 |
| CA38 | Control de acceso por permisos | **Parcial** | La web no decide el acceso: lo decide la API en cada petición, y ante un 403 cierra la sesión. Pero hoy el backend autoriza por **perfil**, no por los permisos asignados | `SeguridadEstadosTest` (403 y 401) | W-03 |

## HU10 — Reporte PDF

| Id | Criterio | Estado | Cómo se cumple en la web | Prueba automática | Evidencia |
|---|---|---|---|---|---|
| CA39 | Selección del filtro de estado | Cumple | Opciones «Requerido» y «No requerido»; sin un estado válido no se pide el reporte | `ReportesTest.pantallaTieneElFiltroYLosBotonesActivos` | W-15, W-16 |
| CA40 | Resultados del filtro | Cumple | La web envía el estado elegido a la API; el filtrado lo hace el backend | `ClientesRestTest` (reporte) | [Evidencias del backend](../../backend/docs/evidencias/) |
| CA41 | Contenido del reporte | Cumple | El PDF lo construye el backend con identificación, nombres y apellidos; la web lo entrega sin modificarlo | `ReportesTest.descargarDevuelveElPdfComoAdjuntoSinCache` | [Evidencias del backend](../../backend/docs/evidencias/) |
| CA42 | Exportación en PDF | Cumple | «Ver PDF» y «Descargar PDF» | `ReportesTest.verAbreElPdfEnElNavegador` | Humo C03 |
| CA43 | Reporte sin resultados | **Parcial** | La web informa «No hay ciudadanos con estado «…». No se generó el reporte.», pero no entrega un PDF que lo diga | `ReportesTest.sinResultadosVuelveConMensajeClaro` | W-17 |

## Transversal

| Id | Criterio | Estado | Cómo se cumple en la web | Prueba automática | Evidencia |
|---|---|---|---|---|---|
| CA44 | Cifrado de contraseñas | Cumple | Las contraseñas las cifra el backend (BCrypt). La web nunca las guarda ni las muestra: no hay campo de contraseña al crear usuarios y el token queda en el servidor web | `UsuariosYPerfilesTest.formularioDeCreacionNoPideContrasena`, `SeguridadRestTest.crearEnviaCincoCamposSinEstadoNiContrasena` | Humo U05 |

## Pendientes que dependen del backend

La web consume la API y no accede a la base de datos, así que estos criterios no se pueden cerrar solo desde `web/`.

| Criterio | Qué falta | Quién | Qué haría la web después |
|---|---|---|---|
| CA35 | Endpoints para crear y modificar perfiles (`POST` y `PUT /api/perfiles`). Hoy el catálogo es fijo | Backend y seguridad | Agregar el formulario de perfil en la pestaña «Perfiles y permisos» |
| CA36 | Un endpoint que publique las opciones y sus permisos (`GET /api/opciones`). Las tablas `opcion` y `opcion_permiso` ya existen | Backend | Leer las opciones de la API en lugar de deducirlas del código del permiso |
| CA38 | Que la autorización use los permisos del perfil. Hoy `SeguridadConfig` decide por perfil (`hasRole`), así que cambiar los permisos de un perfil queda guardado pero no cambia lo que sus usuarios pueden hacer | Seguridad | Nada: la web ya obedece el 403 de la API |
| CA43 | Decidir si «sin resultados» debe ser un PDF que lo indique. Hoy la API responde 404 `SIN_RESULTADOS` y la web muestra el mensaje | Backend | Entregar ese PDF igual que los demás |
