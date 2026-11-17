# Checklist final del proyecto

Estados basados en las evidencias disponibles en el repositorio. **Hecho** indica
que existe implementación o verificación documentada; **Parcial** indica
verificación incompleta o limitada al tipo de prueba indicado; **Falta** marca
trabajo que aún no tiene evidencia de ejecución.

| Ítem | Estado | Evidencia |
|---|---|---|
| Repositorio integrado y documentado | Parcial | [README raíz](../../README.md) con índice de documentación; pruebas de backend y web; faltan las verificaciones manuales indicadas abajo, las etiquetas de versión y el paso a `main` |
| Instalación reproducible de backend, web y móvil | Hecho | [backend/README.md](../backend/README.md), [web/README.md](../web/README.md), [mobile/README.md](../mobile/README.md); comandos `./mvnw` y `npm ci` |
| Base reconstruible desde cero | Hecho | [backend/docs/modelo-datos/reconstruccion-base.md](../backend/docs/modelo-datos/reconstruccion-base.md), [backend/compose.yaml](../backend/compose.yaml), Flyway `V1`–`V7`, `V100` y `V101` |
| Health responde 200 | Hecho | [salida del smoke test](../backend/docs/evidencias/smoke-test-salida.txt): `PASS: health 200` |
| Web registra, lista, busca y actualiza ciudadanos | Parcial | Suite web: `RegistroCiudadanoTest`, `ConsultaYEdicionTest`, `ClientesRestTest`; los tests no sustituyen una comprobación end-to-end manual en navegador contra el backend |
| Móvil: login, escaneo y resultado | Hecho | [mobile/docs/pruebas.md](../mobile/docs/pruebas.md): 12 pruebas de integración contra el backend real y 14 casos manuales aprobados (emulador y teléfono físico) con capturas en [mobile/docs/evidencias/](../mobile/docs/evidencias/) |
| PDF con ambos filtros y caso sin resultados | Hecho | [salida del smoke test](../backend/docs/evidencias/smoke-test-salida.txt): ambos PDF válidos y 404 `SIN_RESULTADOS` |
| Autorización 401/403 | Hecho | [salida del smoke test](../backend/docs/evidencias/smoke-test-salida.txt), matrices parametrizadas en `SeguridadIntegracionTest` y 136 pruebas backend aprobadas (última ejecución: 2026-11-16) |
| Sin secretos versionados ni datos personales reales | Hecho | `.gitignore`, `.env.example`, datos ficticios en `backend/src/main/resources/db/demo/`; se comprobó que no hay `.env` real en el índice con `git ls-files | grep -E '(^|/)\.env$'` |
| Simulaciones identificadas | Hecho | [registro de supuestos](supuestos.md), OpenAPI y README de backend etiquetan Defensa, correo y datos de demo como simulados |
| Evidencias de pruebas | Hecho | [backend/docs/evidencias/](../backend/docs/evidencias/), [mobile/docs/evidencias/](../mobile/docs/evidencias/), reportes locales `backend/target/surefire-reports/` y `web/target/surefire-reports/` |
| Actas de reuniones | Hecho — fuera del repositorio | `00_Consolidado_Entrega.xlsx` (entrega, fuera del repositorio), hoja «Actas»: ceremonias de los tres sprints y sesión de integración, elaboradas a partir de los acuerdos del equipo y del historial del repositorio |
| Backlog y mapa de historias | Hecho — fuera del repositorio | `00_Consolidado_Entrega.xlsx` (entrega, fuera del repositorio), hojas «Product_Backlog», «Story_Map» y «Sprint_Backlog» |
| Estimación PERT y burndown | Hecho — fuera del repositorio | `00_Consolidado_Entrega.xlsx` (entrega, fuera del repositorio), hojas «PERT», «Capacidad_Equipos», «Planning_Poker», «Gantt» y «Burndown» |
| Pitch y simulacros | Parcial — Pendiente equipo | `Presentacion_Ejecutiva.pptx` en la entrega; no hay evidencia registrada de simulacros de sustentación |
| Pruebas manuales en celular | Hecho | Casos con cámara M-10 a M-12 ejecutados en un teléfono físico con Expo Go; capturas `M-09c`, `M-10`, `M-11`, `M-12a`, `M-12b` y `M-13b` en [mobile/docs/evidencias/](../mobile/docs/evidencias/) |
| Guía de uso (correr, correo, registro, inicio de sesión, errores) | Hecho | [guia-de-uso.md](guia-de-uso.md) |
| RNF de rendimiento medidos | Hecho | [backend/docs/pruebas/rendimiento.md](../backend/docs/pruebas/rendimiento.md): RNF-R1 y RNF-R2 cumplen con 30 muestras por operación (`scripts/medir-rendimiento.sh`) |
| Supuestos en un registro único | Hecho | [supuestos.md](supuestos.md): simulaciones, datos y API, seguridad y app móvil, con identificadores sin repetir |
| Código documentado | Hecho | Javadoc de clase en backend (89 de 89) y web (39 de 41); comentarios de documentación en los módulos del móvil |
| Smoke test PowerShell | Hecho | Ejecutado con PowerShell 7.7.0 en Linux; la validación en Windows es opcional. [Salida completa](../backend/docs/evidencias/smoke-test-salida-powershell.txt): todas las comprobaciones pasaron |
| OWASP ZAP baseline | Hecho (escaneo pasivo, alcance anónimo) | [Salida completa](../backend/docs/evidencias/zap-baseline-salida.txt), [procedimiento y validación](../backend/docs/pruebas/zap-baseline.md): 67 PASS, 0 FAIL, 1 WARN validado manualmente |

La planeación con Scrum, la especificación de casos de uso y la trazabilidad no se
versionan aquí: se entregan en el libro de Excel consolidado. El repositorio guarda
el código y su documentación técnica.
