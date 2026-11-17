# Resultados de pruebas y seguridad

## Resultados medidos

Conteos tomados de los archivos `target/surefire-reports/*.txt` presentes en
este checkout, no de una estimación ni de una ejecución anterior.

| Módulo / clase de prueba | Ejecutadas | Pasan | Fallan | Errores | Omitidas |
|---|---:|---:|---:|---:|---:|
| Backend — `AntecedenteServiceTest` | 1 | 1 | 0 | 0 | 0 |
| Backend — `ApplicationSmokeTest` | 1 | 1 | 0 | 0 | 0 |
| Backend — `ArchitectureTest` | 5 | 5 | 0 | 0 | 0 |
| Backend — `SeguridadIntegracionTest` | 113 | 113 | 0 | 0 | 0 |
| Backend — `GeneradorContrasenaTest` | 2 | 2 | 0 | 0 | 0 |
| Backend — `CiudadanoControllerTest` | 1 | 1 | 0 | 0 | 0 |
| Backend — `CiudadanoRepositoryTest` | 2 | 2 | 0 | 0 | 0 |
| Backend — `CiudadanoServiceTest` | 4 | 4 | 0 | 0 | 0 |
| Backend — `CorsConfigTest` | 1 | 1 | 0 | 0 | 0 |
| Backend — `PersonalServiceTest` | 1 | 1 | 0 | 0 | 0 |
| Backend — `RegistroPersonalSeguridadTest` | 3 | 3 | 0 | 0 | 0 |
| Backend — `ReporteServiceTest` | 2 | 2 | 0 | 0 | 0 |
| **Backend — total Surefire** | **136** | **136** | **0** | **0** | **0** |
| Web — `AccesoYEstadosTest` | 21 | 21 | 0 | 0 | 0 |
| Web — `ConsultaYEdicionTest` | 9 | 9 | 0 | 0 | 0 |
| Web — `DefectosCorregidosTest` | 6 | 6 | 0 | 0 | 0 |
| Web — `RegistroCiudadanoTest` | 10 | 10 | 0 | 0 | 0 |
| Web — `ReportesTest` | 6 | 6 | 0 | 0 | 0 |
| Web — `VistasTest` | 12 | 12 | 0 | 0 | 0 |
| Web — `ClientesRestTest` | 19 | 19 | 0 | 0 | 0 |
| **Web — total Surefire** | **83** | **83** | **0** | **0** | **0** |

Los 113 casos de `SeguridadIntegracionTest` incluyen instancias de pruebas
parametrizadas. Los detalles de cada escenario y su método están en el
[plan de pruebas](./plan-de-pruebas.md). No hay casos omitidos en los reportes.
La salida completa del smoke test Bash está en
[evidencias/smoke-test-salida.txt](../evidencias/smoke-test-salida.txt); reporta
health, autenticación, CRUD de ciudadano, registro con Defensa SIMULADO y
Mailpit, antecedentes, PDF con y sin resultados, perfiles/usuarios y negativos
como aprobados. El smoke test PowerShell se ejecutó en Linux con PowerShell
7.7.0 y todas las comprobaciones pasaron. La salida completa, sin tokens ni
contraseñas, está en
[evidencias/smoke-test-salida-powershell.txt](../evidencias/smoke-test-salida-powershell.txt).
La validación en Windows es opcional; se usaron las URLs configurables del
script para el backend local y Mailpit aislado.

Las pruebas móviles, en especial las que dependen de cámara/dispositivo, se
registran manualmente en
[mobile/docs/pruebas.md](../../../mobile/docs/pruebas.md); no se incluyen en
los totales Surefire.

## Hallazgos

| Severidad | Estado actual | Hallazgo y prueba/evidencia |
|---|---|---|
| Alta | Corregido y cubierto | Los tokens anteriores a un cambio de perfil o estado debían quedar invalidados. `SeguridadIntegracionTest#administraUsuariosConHashEstadoYRevocacionDeTokens` verifica la revocación por versión de seguridad. |
| Media | Corregido y cubierto | El bloqueo por contraseña incorrecta debía activarse al sexto fallo durante quince minutos. `SeguridadIntegracionTest#bloqueaEnElSextoIntentoYSeDesbloqueaTrasQuinceMinutos` lo verifica con reloj controlado. |
| Media | Corregido y cubierto | El registro de personal necesitaba limitar intentos repetidos por IP. `SeguridadIntegracionTest#limitaIntentosRepetidosDeRegistroPorIp` comprueba el rechazo tras el límite configurado. |
| Media | Corregido y cubierto | CORS no debe aceptar comodines y Swagger/actuator no deben exponerse fuera de `dev`. `CorsConfigTest#rechazaOrigenesCorsConComodin` y `SeguridadIntegracionTest#aplicaCabecerasDeSeguridadYNoExponeDocumentacionFueraDeDev` pasan. |
| Media | Corregido; revisar despliegues externos | Se retiró la contraseña por defecto de Compose; `DB_PASSWORD` se exige en el entorno local. Evidencia: `proyecto/backend/compose.yaml`, `.env.example` y configuración de aplicación. |
| Aviso (la salida no asigna severidad) | Ejecutado; sin hallazgo accionable | OWASP ZAP baseline: 67 reglas PASS, `FAIL-NEW=0` y `WARN-NEW=1`; salida completa en [zap-baseline-salida.txt](../evidencias/zap-baseline-salida.txt). El aviso `Non-Storable Content` afecta `/`, `/robots.txt` y `/sitemap.xml`, que responden 401 con `Cache-Control: no-cache, no-store, max-age=0, must-revalidate`; validación manual: el contenido no es almacenable. El spider no pudo explorar la raíz sin autenticación (401), por lo que la cobertura fue anónima. |
| Baja | Pendiente de operación/equipo | Revisar en el despliegue real la rotación y gestión de secretos, acceso/retención de logs y dependencias con análisis actualizado. Las pruebas automatizadas no sustituyen esa revisión. |

## Trazabilidad

Los nombres exactos de las pruebas, el criterio de cada caso y su estado se
mantienen en el [plan de pruebas](./plan-de-pruebas.md). El contrato del smoke
test Bash y PowerShell está documentado por sus salidas guardadas; la validación
en Windows es opcional y los casos manuales de cámara siguen pendientes. ZAP
terminó con código 2 por el aviso, sin alertas `FAIL`; véase el procedimiento y
alcance en
[zap-baseline.md](./zap-baseline.md).
