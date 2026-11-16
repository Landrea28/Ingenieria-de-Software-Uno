# Pruebas de la app móvil — casos y resultados

Casos de prueba del módulo móvil según la sección 12 del plan, con el resultado de su ejecución. Todos los datos son ficticios (dataset demo del backend, fuente `SIMULADA`).

Hay tres niveles:

1. **Unitarias** (`npm test`): lógica pura de validaciones, vigencia de sesión y extracción del código de barras.
2. **Integración contra el backend real** (`npm run test:api`): el mismo cliente HTTP y los mismos servicios que usan las pantallas (`src/api`), contra el backend con PostgreSQL y Mailpit. No usa el servidor falso.
3. **Manuales en emulador**: lo que depende de la interfaz, la cámara y el almacenamiento seguro del teléfono.

## Resumen de la ejecución

| Nivel | Fecha | Casos | Resultado |
|---|---|---|---|
| Unitarias | 2026-11-15 | 14 | 14 aprobadas, 0 fallidas |
| Verificación de tipos (`npm run typecheck`) | 2026-11-15 | — | Sin errores |
| Integración contra backend real | 2026-11-15 | 12 | 12 aprobadas, 0 fallidas |
| Manuales en emulador | 2026-11-15 | 14 | 11 aprobadas, 0 fallidas, 3 pendientes (escaneo con cámara) |

Entorno de la integración: backend del repositorio en `develop` (commit `e8c409d`) con perfiles `demo,dev` sobre una base limpia, PostgreSQL 17 y Mailpit con `docker compose`, Node.js 24.

## 1. Pruebas unitarias

| Archivo | Qué cubre |
|---|---|
| `src/utils/validaciones.test.ts` | Registro correcto, campos obligatorios vacíos, identificación de 6 a 10 dígitos, formato de correo y caracteres admitidos en nombres y apellidos |
| `src/utils/tiempo.test.ts` | Cálculo del vencimiento de la sesión, token vigente, vencido o con fecha inválida, y lectura del vencimiento de un JWT |
| `src/utils/codigoBarras.test.ts` | Extracción de la identificación: valor simple, con separadores, ruido no numérico y longitudes fuera de rango (supuesto MOV-08) |

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

## 3. Pruebas manuales en emulador

Ejecutadas el 2026-11-15 en el emulador Android (dispositivo virtual `Medium_Phone_API_37.0`, Android 17, Expo Go con SDK 58), con `EXPO_PUBLIC_API_MODO=real` contra el backend con perfiles `demo,dev`. La pantalla se manejó por `adb` y cada caso tiene su captura en [`evidencias/`](evidencias/).

| # | Funcionalidad | Pasos | Resultado obtenido | Estado | Evidencia |
|---|---|---|---|---|---|
| M-01 | Registro | Enviar el formulario vacío | Cada campo obligatorio muestra su mensaje; no se envía nada | OK | [captura](evidencias/M-01-registro-campos-obligatorios.png) |
| M-02 | Registro | Elegir Policía y el rango Subintendente; cambiar a Ejército | Con Policía se ofrecen Intendente, Patrullero y Subintendente; al cambiar de fuerza el rango se descarta y se ofrecen Cabo, Sargento y Soldado | OK | [a](evidencias/M-02a-rango-de-policia.png), [b](evidencias/M-02b-rangos-de-ejercito.png) |
| M-03 | Registro | Registrar la identidad `8000000005` del dataset de Defensa | «Registro aceptado»; la app avisa que la contraseña llega por correo y no la muestra; el correo `[SIMULADO] Credenciales de acceso` llegó a Mailpit | OK | [a](evidencias/M-03a-registro-diligenciado.png), [b](evidencias/M-03b-registro-aceptado.png) |
| M-04 | Registro | Registrar `8000000099`, que no está en Defensa | «Registro rechazado: Los datos no pudieron ser validados» | OK | [captura](evidencias/M-04-registro-rechazado.png) |
| M-05 | Login | Contraseña incorrecta | «Identificación o contraseña incorrectas.»; sigue en Login y el campo de contraseña se vacía | OK | [captura](evidencias/M-05-login-credenciales-invalidas.png) |
| M-06 | Recordar contraseña | Ingresar con la opción activa, forzar el cierre de la app y abrirla | Entra directo a Inicio sin pedir credenciales | OK | [a](evidencias/M-06a-login-con-recordar.png), [b](evidencias/M-06b-inicio-con-sesion.png), [c](evidencias/M-06c-reabrir-entra-directo.png) |
| M-07 | Recordar contraseña | Ingresar con la opción desactivada, forzar el cierre y abrir | Pide iniciar sesión | OK | [a](evidencias/M-07a-login-sin-recordar.png), [b](evidencias/M-07b-sin-recordar-pide-login.png) |
| M-08 | Sesión | Tocar **Cerrar sesión** con recordar activo y reabrir la app | Pide iniciar sesión (el token se borró) | OK | [captura](evidencias/M-08-cerrar-sesion-pide-login.png) |
| M-09 | Escáner | Negar el permiso de cámara (dos veces) | Primero ofrece **Conceder permiso**; tras la denegación definitiva ofrece **Abrir ajustes**; el ingreso manual sigue disponible | OK | [a](evidencias/M-09a-solicitud-permiso.png), [b](evidencias/M-09b-permiso-denegado.png) |
| M-10 | Escáner | Escanear con la cámara el código `9000000003` | — | Pendiente | — |
| M-11 | Escáner | Escanear con la cámara el código `9000000001` | — | Pendiente | — |
| M-12 | Escáner | Escanear con la cámara el código `NO-ES-CEDULA` | — | Pendiente | — |
| M-13 | Consulta | Ingresar manualmente `9000000020` | Aviso de sin información, que aclara que no equivale a «No requerido» | OK | [captura](evidencias/M-13-manual-sin-informacion.png) |
| M-14 | Red | Apagar el backend y consultar; encenderlo y tocar **Reintentar** | «No hay conexión con el servidor…» con **Reintentar**; al volver el backend la consulta responde | OK | [a](evidencias/M-14a-sin-conexion.png), [b](evidencias/M-14b-reintentar-con-servidor.png) |

Resultado: 11 de 14 casos aprobados y 3 pendientes.

**Casos pendientes (M-10 a M-12).** La cámara del emulador muestra una escena virtual (una habitación) y no fue posible poner el código de barras frente a ella, así que la lectura real con cámara no se probó. Sí se comprobó que con el permiso concedido el escáner abre la cámara con el marco guía ([captura](evidencias/escaner-camara-activa.png)), y que el resto del flujo funciona con ingreso manual: `9000000003` muestra **REQUERIDO** ([captura](evidencias/consulta-manual-requerido.png)) y `9000000001` muestra **NO REQUERIDO** ([captura](evidencias/consulta-manual-no-requerido.png)). La extracción de la identificación desde el texto leído está cubierta por las pruebas unitarias. Estos tres casos deben ejecutarse en un teléfono físico con los códigos de [codigos-prueba.md](codigos-prueba.md).

### Defectos encontrados y corregidos

| Defecto | Dónde | Corrección |
|---|---|---|
| Las peticiones al backend expiraban en Android 17 porque Expo Go no tenía el permiso de red local | Entorno del emulador | Conceder el permiso (documentado en el README) |
| Mensajes con falta de concordancia: «Los nombres es obligatorio», «Los apellidos es obligatorio» | `src/utils/validaciones.ts` | Ahora dicen «son obligatorios» y «admiten máximo» |
| Tras un registro aceptado, **Ir a iniciar sesión** apilaba un segundo Login con flecha de volver hacia la pantalla de registro aceptado | `src/screens/RegistroScreen.tsx` | Se usa `popTo('Login')` para regresar al Login existente |

Las dos correcciones de código se verificaron de nuevo en el emulador después de aplicarlas.

Para comprobar que la contraseña no queda en el dispositivo (M-06): la app solo llama a `expo-secure-store` desde `src/storage/sesion.ts`, que guarda el token y su vencimiento; no usa AsyncStorage.
