# Pruebas de la app móvil — casos y resultados

Casos de prueba del módulo móvil según la sección 12 del plan, con el resultado de su ejecución. Todos los datos son ficticios (dataset demo del backend, fuente `SIMULADA`).

Hay tres niveles:

1. **Unitarias** (`npm test`): lógica pura de validaciones, vigencia de sesión y extracción del código de barras.
2. **Integración contra el backend real** (`npm run test:api`): el mismo cliente HTTP y los mismos servicios que usan las pantallas (`src/api`), contra el backend con PostgreSQL y Mailpit. No usa el servidor falso.
3. **Manuales en dispositivo o emulador**: lo que depende de la interfaz, la cámara y el almacenamiento seguro del teléfono.

## Resumen de la ejecución

| Nivel | Fecha | Casos | Resultado |
|---|---|---|---|
| Unitarias | 2026-11-15 | 14 | 14 aprobadas, 0 fallidas |
| Verificación de tipos (`npm run typecheck`) | 2026-11-15 | — | Sin errores |
| Integración contra backend real | 2026-11-15 | 12 | 12 aprobadas, 0 fallidas |
| Manuales en dispositivo | Pendiente | 14 | Sin ejecutar (ver tabla al final) |

Entorno de la integración: backend del repositorio en `develop` (commit `e8c409d`) con perfiles `demo,dev` sobre una base limpia, PostgreSQL 17 y Mailpit con `docker compose`, Node.js 24.

## 1. Pruebas unitarias

| Archivo | Qué cubre |
|---|---|
| `src/utils/validaciones.test.ts` | Registro correcto, campos obligatorios vacíos, identificación de 6 a 10 dígitos, formato de correo y caracteres admitidos en nombres y apellidos |
| `src/utils/tiempo.test.ts` | Cálculo del vencimiento de la sesión, token vigente, vencido o con fecha inválida, y lectura del vencimiento de un JWT |
| `src/utils/codigoBarras.test.ts` | Extracción de la identificación: valor simple, con separadores, ruido no numérico y longitudes fuera de rango (supuesto S-10) |

## 2. Integración contra el backend real

Archivo: [`pruebas-api/flujo-real.ts`](../pruebas-api/flujo-real.ts). Persona de prueba: identidad SIMULADA `8000000005` del dataset de Defensa.

| # | Funcionalidad | Caso | Resultado esperado | Criterios | Estado |
|---|---|---|---|---|---|
| I-01 | Catálogo | Carga pública de fuerzas y rangos | Cuatro fuerzas, cada una con sus rangos | CA01 | OK |
| I-02 | Registro | Datos que Defensa no valida | 400 `VALIDACION` con mensaje genérico | CA09 | OK |
| I-03 | Registro | Campos con formato inválido | 400 con el detalle de cada campo | CA02 | OK |
| I-04 | Registro | Datos que coinciden con Defensa | Respuesta solo con un mensaje; al correo de pruebas llega una contraseña de exactamente 6 caracteres con mayúscula, minúscula y número; la API no la devuelve | CA04–CA08 | OK |
| I-05 | Registro | Misma identificación otra vez | 409 `DUPLICADO` | CA03 | OK |
| I-06 | Login | Contraseña incorrecta | 401 con mensaje genérico; no se trata como sesión vencida | CA13 | OK |
| I-07 | Permisos | Consultar antecedentes sin token | 401 | — | OK |
| I-08 | Login y recordar | Credenciales válidas, con y sin «Recordar contraseña» | Token `Bearer` con vigencia; con recordar la vigencia es mayor; la respuesta no trae la contraseña | CA10–CA12, CA14 | OK |
| I-09 | Consulta | Ciudadanos `9000000003` y `9000000001` | `REQUERIDO` y `NO_REQUERIDO`, fuente `SIMULADA` | CA28, CA29 | OK |
| I-10 | Consulta | Ciudadano sin estado (`9000000020`) e inexistente | 404 `SIN_INFORMACION`, nunca `NO_REQUERIDO` | CA30 | OK |
| I-11 | Logout | Cerrar sesión y volver a consultar | Token revocado: 401 y la app vuelve al login | — | OK |
| I-12 | Red | Servidor inalcanzable | Error de red con mensaje propio | — | OK |

Comprobaciones adicionales hechas sobre la base de la prueba: la contraseña del usuario registrado está guardada como hash BCrypt (`$2a$10$…`, 60 caracteres) y no aparece en el log del backend.

### Cómo repetirla

```bash
# en proyecto/backend, con las variables de .env.example exportadas
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=demo,dev

# en proyecto/mobile
npm run test:api
```

La prueba registra a la persona `8000000005`, así que necesita una base limpia: en una segunda ejecución sobre la misma base el caso I-04 falla porque la cuenta ya existe. Para repetir sin recrear la base se puede usar otra identidad del dataset con la variable `PRUEBA_IDENTIFICACION` y los datos correspondientes. El backend limita los registros a cinco intentos por IP cada 15 minutos; la prueba hace cuatro.

## 3. Pruebas manuales en dispositivo o emulador

Requieren la app en Expo Go o emulador Android, con `EXPO_PUBLIC_API_MODO=real`. Los códigos de barras están en [codigos-prueba.md](codigos-prueba.md). Guardar una captura por caso en `docs/evidencias/` y anotar el resultado.

| # | Funcionalidad | Pasos | Resultado esperado | Estado |
|---|---|---|---|---|
| M-01 | Registro | Enviar el formulario vacío | Cada campo obligatorio muestra su mensaje; no se envía nada | Pendiente |
| M-02 | Registro | Elegir una fuerza, elegir rango y cambiar de fuerza | El rango se descarta y la lista muestra solo los de la nueva fuerza | Pendiente |
| M-03 | Registro | Registrar una identidad del dataset de Defensa | Estado pendiente y luego aceptado; el correo llega a Mailpit; la app no muestra la contraseña | Pendiente |
| M-04 | Registro | Registrar una identificación que no está en Defensa | Estado rechazado con el motivo | Pendiente |
| M-05 | Login | Contraseña incorrecta | Mensaje de credenciales inválidas; sigue en Login | Pendiente |
| M-06 | Recordar contraseña | Ingresar con la opción activa, cerrar la app por completo y abrirla | Entra directo a Inicio sin pedir credenciales | Pendiente |
| M-07 | Recordar contraseña | Ingresar con la opción desactivada, cerrar la app y abrirla | Pide iniciar sesión | Pendiente |
| M-08 | Sesión | Tocar **Cerrar sesión** con recordar activo y reabrir la app | Pide iniciar sesión (el token se borró) | Pendiente |
| M-09 | Escáner | Negar el permiso de cámara | Aviso con **Conceder permiso** o **Abrir ajustes**, e ingreso manual disponible | Pendiente |
| M-10 | Escáner | Escanear el código `9000000003` | Insignia **REQUERIDO** | Pendiente |
| M-11 | Escáner | Escanear el código `9000000001` | Insignia **NO REQUERIDO** | Pendiente |
| M-12 | Escáner | Escanear el código `NO-ES-CEDULA` | Aviso de código no legible, con **Reintentar** e ingreso manual | Pendiente |
| M-13 | Consulta | Ingresar manualmente `9000000020` | Aviso de sin información, distinto de «No requerido» | Pendiente |
| M-14 | Red | Apagar el backend y consultar | Mensaje de sin conexión con **Reintentar** | Pendiente |

Para comprobar que la contraseña no queda en el dispositivo (M-06): la app solo llama a `expo-secure-store` desde `src/storage/sesion.ts`, que guarda el token y su vencimiento; no usa AsyncStorage.
