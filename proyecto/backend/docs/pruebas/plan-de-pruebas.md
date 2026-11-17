# Plan de pruebas

## Alcance y reglas de ejecución

- Backend: pruebas JUnit/Spring del módulo. Estado medido en
  `proyecto/backend/target/surefire-reports/` mediante `./mvnw clean verify`.
- Web: pruebas del módulo Spring MVC/Thymeleaf, medidas en
  `proyecto/web/target/surefire-reports/` mediante `./mvnw clean verify`.
- Smoke test de API real: `proyecto/scripts/smoke-test.sh`; la salida conservada
  está en [evidencias/smoke-test-salida.txt](../evidencias/smoke-test-salida.txt).
- Móvil: las pruebas de unidad, integración y dispositivo son manuales o se
  ejecutan con los comandos descritos en
  [mobile/docs/pruebas.md](../../../mobile/docs/pruebas.md). No forman parte de
  Surefire. El archivo `smoke-test.ps1` aún no se ha ejecutado en Windows.

## Casos y estado

Los nombres de métodos se refieren a `SeguridadIntegracionTest` salvo que se
indique la clase completa. Los casos parametrizados se expanden en Surefire; el
resultado total reportado para cada suite aparece en la sección de resultados.

| Área | Caso / criterio | Prueba (`clase#método`) | Estado observado |
|---|---|---|---|
| Registro personal | Defensa SIMULADO acepta; persiste el hash y no expone la contraseña | `SeguridadIntegracionTest#registroValidaDefensaGuardaHashYNoExponeLaContrasena` | Pasó |
| Registro personal | Identificación inexistente y datos que no coinciden no se pueden enumerar | `SeguridadIntegracionTest#identificacionInexistenteYDatosNoCoincidentesDevuelvenElMismoError` | Pasó |
| Registro personal | Inyección SQL, formatos, longitudes excesivas y JSON malformado responden 400 sin stack trace | `SeguridadIntegracionTest#entradasManipuladasEnLoginYRegistroSiempreDevuelven400SinStackTrace` (parametrizada) | Pasó |
| Registro personal | Límite de registros por IP | `SeguridadIntegracionTest#limitaIntentosRepetidosDeRegistroPorIp` | Pasó |
| Integración de registro | Rechazo de Defensa, indisponibilidad de Defensa y correo no disponible son controlados | `RegistroPersonalSeguridadTest#defensaRechazadaProduceRespuestaControlada400`; `RegistroPersonalSeguridadTest#defensaNoDisponibleProduceRespuestaControlada503Nunca500`; `RegistroPersonalSeguridadTest#correoNoDisponibleProduceRespuestaControlada503Nunca500` | Pasó |
| Contraseña temporal | Longitud exacta de seis caracteres y mayúscula, minúscula y dígito | `GeneradorContrasenaTest#generaMilContrasenasDeSeisCaracteresConLasTresClases`; `GeneradorContrasenaTest#validaCasosLimiteDeLaPolitica` | Pasó |
| Secreto de registro | Contraseña generada y hash ausentes de respuestas y logs | `SeguridadIntegracionTest#registroValidaDefensaGuardaHashYNoExponeLaContrasena`; `SeguridadIntegracionTest#administraUsuariosConHashEstadoYRevocacionDeTokens` | Pasó |
| Login | Login válido, respuesta genérica ante credenciales incorrectas y formatos inválidos | `SeguridadIntegracionTest#loginValidoDevuelveTokenYCredencialesInvalidasSonGenericas`; `SeguridadIntegracionTest#entradasManipuladasEnLoginYRegistroSiempreDevuelven400SinStackTrace` | Pasó |
| Bloqueo de login | Bloqueo al sexto intento y desbloqueo al transcurrir quince minutos | `SeguridadIntegracionTest#bloqueaEnElSextoIntentoYSeDesbloqueaTrasQuinceMinutos` | Pasó |
| Recordar sesión | `recordarContrasena` extiende el TTL del JWT; no persiste la contraseña | `SeguridadIntegracionTest#recordarExtiendeLaVidaDelTokenYLasRutasPublicasSiguenAbiertas` | Pasó |
| JWT y roles | Lecturas protegidas × sin token, inválido, vencido, revocado, PERSONAL y ADMINISTRADOR | `SeguridadIntegracionTest#matrizDeLecturasProtegidasCubreTodosLosTiposDeTokenYRoles` (parametrizada) | Pasó |
| JWT y roles | Escrituras protegidas × los mismos seis estados; las operaciones permitidas por ADMINISTRADOR completan | `SeguridadIntegracionTest#matrizParametrizadaDeEscriturasProtegidas` (parametrizada) | Pasó |
| Política de rutas | Cada mapeo `/api/**` tiene política de acceso declarada | `SeguridadIntegracionTest#cadaEndpointApiTieneUnaPoliticaDeAccesoDeclarada` | Pasó |
| Antecedentes | Ausencia de antecedente no se interpreta como NO_REQUERIDO; ausencia de información responde 404 | `SeguridadIntegracionTest#antecedentesSinEstadoOInexistentesDevuelven404ParaPersonalAutenticado`; `AntecedenteServiceTest#ciudadanoSinEstadoNoSeClasificaComoNoRequerido`; `CiudadanoRepositoryTest#diferenciaFaltaDeAntecedentes` | Pasó |
| Reportes | PDF con resultados y 404 `SIN_RESULTADOS` sin resultados; solo ADMINISTRADOR | `SeguridadIntegracionTest#reportesRespondenPdfParaResultadosY404SinResultadosSoloParaAdministrador`; `ReporteServiceTest#generaArchivoPdfReal`; `ReporteServiceTest#noGeneraPdfVacio` | Pasó |
| Usuarios y perfiles | CRUD de usuarios, hash, cambios de estado/perfil y revocación | `SeguridadIntegracionTest#administraUsuariosConHashEstadoYRevocacionDeTokens` | Pasó |
| Permisos | PERSONAL recibe 403; ADMINISTRADOR no puede quitarse el permiso requerido ni autodesactivarse | `SeguridadIntegracionTest#protegePerfilesYNoPermiteRetirarGestionAlAdministradorNiAutodesactivarse` | Pasó |
| Cabeceras y documentación | Cabeceras seguras; Swagger/actuator no expuestos fuera de `dev` | `SeguridadIntegracionTest#aplicaCabecerasDeSeguridadYNoExponeDocumentacionFueraDeDev`; `CorsConfigTest#rechazaOrigenesCorsConComodin` | Pasó |
| Web | Login, permisos, sesión y respuestas 401/403 | `AccesoYEstadosTest#loginExitosoEntraAlPanelYMuestraElUsuario`; `AccesoYEstadosTest#usuarioSinRolAdministradorNoPuedeEntrar`; `AccesoYEstadosTest#api401EnUnaPantallaCierraLaSesionYVuelveAlLogin`; `AccesoYEstadosTest#api403EnUnaPantallaVuelveAlLoginConMensaje` | Pasó |
| Web | Registrar ciudadanos, validación de los diez campos y duplicados | `RegistroCiudadanoTest#registroValidoConfirmaYApareceEnElListado`; `RegistroCiudadanoTest#formularioVacioMarcaLosDiezCamposObligatorios`; `RegistroCiudadanoTest#identificacionDuplicadaMuestraErrorEnElCampo`; `RegistroCiudadanoTest#estaturaFueraDeRangoOConTresDecimalesEsRechazada` | Pasó |
| Web | Listar, buscar, consultar y editar ciudadanos | `ConsultaYEdicionTest#consultaPorIdentificacionVaAlDetalleAunqueTengaPuntosOEspacios`; `ConsultaYEdicionTest#detalleMuestraLosDiezCampos`; `ConsultaYEdicionTest#edicionValidaGuardaYConfirmaEnElDetalle`; `ConsultaYEdicionTest#edicionInvalidaNoGuardaYMuestraLosErrores` | Pasó |
| Web | Mostrar filtros, descargar/ver PDF y manejar 404 sin resultados | `ReportesTest#pantallaTieneElFiltroYLosBotonesActivos`; `ReportesTest#descargarDevuelveElPdfComoAdjuntoSinCache`; `ReportesTest#verAbreElPdfEnElNavegador`; `ReportesTest#sinResultadosVuelveConMensajeClaro`; `ClientesRestTest#reporteSinResultadosSeDistingueDeRutaInexistente`; `ClientesRestTest#reporteQueNoEsPdfSeRechaza` | Pasó |
| Web | Contrato del cliente REST: autenticación, CRUD, traducción de errores y PDF | `ClientesRestTest#listarEnviaTokenParametrosYLeeLaPagina`; `ClientesRestTest#registrarEnviaLosDiezCamposSinIdYDevuelveElCreado`; `ClientesRestTest#actualizarHacePutConNueveCamposSinIdentificacionNiId`; `ClientesRestTest#error401Y403SeTraducen`; `ClientesRestTest#reporteDevuelveLosBytesDelPdf` | Pasó |
| API real | Health, login, ciudadano, registro personal, Mailpit, antecedentes, PDF, perfiles/usuarios y negativos | `proyecto/scripts/smoke-test.sh`; salida íntegra en [evidencia](../evidencias/smoke-test-salida.txt) | Pasó según salida guardada |
| Windows | Repetir el smoke test con PowerShell contra la aplicación real | `proyecto/scripts/smoke-test.ps1` | Pendiente; no ejecutado en Windows |
| Móvil | Flujo, interfaz, cámara, permisos, almacenamiento seguro y errores de red | Casos manuales en [mobile/docs/pruebas.md](../../../mobile/docs/pruebas.md) | Parcial; quedan pendientes los casos de escaneo indicados allí |

## Repetición

```bash
cd proyecto/backend
./mvnw clean verify

cd ../web
./mvnw clean verify
```

Para repetir el smoke test en Bash, iniciar backend, PostgreSQL y Mailpit y
definir en la sesión `SMOKE_ADMIN_IDENTIFICACION` y `SMOKE_ADMIN_PASSWORD`.
Luego ejecutar `bash proyecto/scripts/smoke-test.sh` desde la raíz del repo.
En Windows, ejecutar `proyecto/scripts/smoke-test.ps1` en PowerShell con las
mismas variables de entorno; esa ejecución sigue pendiente.
