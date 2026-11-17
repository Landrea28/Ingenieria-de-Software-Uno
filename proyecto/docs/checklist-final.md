# Checklist final del proyecto

Estados basados en las evidencias disponibles en el repositorio. **Hecho** indica
que existe implementación o verificación documentada; **Parcial** indica
verificación incompleta o limitada al tipo de prueba indicado; **Falta** marca
trabajo que aún no tiene evidencia de ejecución.

| Ítem | Estado | Evidencia |
|---|---|---|
| Repositorio integrado y documentado | Parcial | README raíz; pruebas de backend y web; faltan entregables del equipo y verificaciones manuales indicadas abajo |
| Instalación reproducible de backend, web y móvil | Hecho | [backend/README.md](../backend/README.md), [web/README.md](../web/README.md), [mobile/README.md](../mobile/README.md); comandos `./mvnw` y `npm ci` |
| Base reconstruible desde cero | Hecho | [backend/docs/modelo-datos/reconstruccion-base.md](../backend/docs/modelo-datos/reconstruccion-base.md), [backend/compose.yaml](../backend/compose.yaml), Flyway `V1`–`V7`, `V100` y `V101` |
| Health responde 200 | Hecho | [salida del smoke test](../backend/docs/evidencias/smoke-test-salida.txt): `PASS: health 200` |
| Web registra, lista, busca y actualiza ciudadanos | Parcial | Suite web: `RegistroCiudadanoTest`, `ConsultaYEdicionTest`, `ClientesRestTest`; los tests no sustituyen una comprobación end-to-end manual en navegador contra el backend |
| Móvil: login, escaneo y resultado | Parcial | [mobile/docs/pruebas.md](../mobile/docs/pruebas.md): integración y pruebas manuales registradas; los tres casos de escaneo M-10–M-12 están pendientes |
| PDF con ambos filtros y caso sin resultados | Hecho | [salida del smoke test](../backend/docs/evidencias/smoke-test-salida.txt): ambos PDF válidos y 404 `SIN_RESULTADOS` |
| Autorización 401/403 | Hecho | [salida del smoke test](../backend/docs/evidencias/smoke-test-salida.txt), matrices parametrizadas en `SeguridadIntegracionTest` y 136 pruebas backend aprobadas |
| Sin secretos versionados ni datos personales reales | Hecho | `.gitignore`, `.env.example`, datos ficticios en `backend/src/main/resources/db/demo/`; se comprobó que no hay `.env` real en el índice con `git ls-files | grep -E '(^|/)\.env$'` |
| Simulaciones identificadas | Hecho | `backend/docs/supuestos.md`, `backend/docs/decisiones/supuestos.md`, OpenAPI y README de backend etiquetan Defensa, correo y datos de demo como simulados |
| Evidencias de pruebas | Hecho | [backend/docs/evidencias/](../backend/docs/evidencias/), [mobile/docs/evidencias/](../mobile/docs/evidencias/), reportes locales `backend/target/surefire-reports/` y `web/target/surefire-reports/` |
| Actas de reuniones | Falta — Pendiente equipo | [plantilla de acta](actas/plantilla-acta.md); registrar reuniones reales |
| Backlog y mapa de historias | Falta — Pendiente equipo | [backlog](planeacion/backlog.md) y [mapa de historias](planeacion/mapa-de-historias.md), plantillas sin datos |
| Estimación PERT y burndown | Falta — Pendiente equipo | [PERT](planeacion/estimacion-pert.md) y [burndown](planeacion/burndown.md), plantillas sin datos |
| Pitch y simulacros | Falta — Pendiente equipo | [estructura del pitch](presentacion/estructura-pitch.md); preparar y ensayar el pitch/simulacros |
| Pruebas manuales en celular | Parcial — Pendiente equipo | [mobile/docs/pruebas.md](../mobile/docs/pruebas.md): completar los casos de escaneo M-10–M-12 en un dispositivo apto |
| Smoke test PowerShell | Hecho | Ejecutado con PowerShell 7.7.0 en Linux; la validación en Windows es opcional. [Salida completa](../backend/docs/evidencias/smoke-test-salida-powershell.txt): todas las comprobaciones pasaron |
| OWASP ZAP baseline | Hecho (escaneo pasivo, alcance anónimo) | [Salida completa](../backend/docs/evidencias/zap-baseline-salida.txt), [procedimiento y validación](../backend/docs/pruebas/zap-baseline.md): 67 PASS, 0 FAIL, 1 WARN validado manualmente |
