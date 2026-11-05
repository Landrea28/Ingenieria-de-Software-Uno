# Resultados de seguridad

Ejecución: `mvn test`, Java 21 y Maven 3.9.9. Los totales se calcularon de los
12 archivos `TEST-*.xml` de `target/surefire-reports`.

| Total | Pasan | Fallan | Errores | Omitidas |
|---:|---:|---:|---:|---:|
| 38 | 38 | 0 | 0 | 0 |

## Hallazgos

| Severidad | Estado | Hallazgo y evidencia |
|---|---|---|
| Alta | Corregido | Un token previo podía conservar acceso después de cambiar perfil o estado. Se agregó `version_seguridad` a usuario y JWT; la prueba `SeguridadIntegracionTest#administraUsuariosConHashEstadoYRevocacionDeTokens` demuestra que esos tokens reciben 401. |
| Media | Corregido | El umbral anterior de cinco fallos no coincidía con el requisito de bloqueo al sexto intento y la restricción de BD permitía solo cinco. La migración V6 eleva el límite; `SeguridadIntegracionTest#bloqueaEnElSextoIntentoYSeDesbloqueaTrasQuinceMinutos` valida el bloqueo y el reloj controlado. |
| Media | Corregido | El registro no limitaba intentos repetidos por IP. V7 persiste la ventana y `SeguridadIntegracionTest#limitaIntentosRepetidosDeRegistroPorIp` comprueba el rechazo controlado tras cinco intentos. |
| Media | Corregido | Un origen CORS con comodín podía habilitarse y Swagger/actuator estaban configurados globalmente. Se restringieron a orígenes explícitos y al perfil `dev`; `CorsConfigTest#rechazaOrigenesCorsConComodin` y `SeguridadIntegracionTest#aplicaCabecerasDeSeguridadYNoExponeDocumentacionFueraDeDev` verifican el límite. |
| Media | Corregido | Compose y la plantilla local contenían la contraseña predeterminada `consulta_local`. Se retiró y Compose exige `DB_PASSWORD`; evidencia en `compose.yaml`, `.env.example` y `application.yml`. |
| Baja | Pendiente | OWASP ZAP baseline es opcional y no se ejecutó en esta validación. Instrucciones en [`zap-baseline.md`](./zap-baseline.md). |
| Baja | Pendiente | Los días 13–14 deben revisar configuración real del despliegue, rotación y gestión de secretos, retención/acceso a logs y dependencias con análisis actualizado. Esta suite no sustituye esa revisión. |

Todos los tests incluidos en esta ejecución están habilitados; no hay `@Disabled`
ni tests omitidos.
