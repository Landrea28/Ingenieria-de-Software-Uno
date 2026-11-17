# Pruebas de la app móvil — casos y resultados

Casos de prueba del módulo móvil según la sección 12 del plan, con el resultado de su ejecución. Todos los datos son ficticios (dataset demo del backend, fuente `SIMULADA`).

Hay tres niveles:

1. **Unitarias** (`npm test`): lógica pura de validaciones, vigencia de sesión y extracción del código de barras.
2. **Integración contra el backend real** (`npm run test:api`): el mismo cliente HTTP y los mismos servicios que usan las pantallas (`src/api`), contra el backend con PostgreSQL y Mailpit. No usa el servidor falso.
3. **Manuales en emulador y teléfono**: lo que depende de la interfaz, la cámara y el almacenamiento seguro. Los que usan la cámara se hicieron en un teléfono físico con Expo Go.

## Resumen de la ejecución

| Nivel | Fecha | Casos | Resultado |
|---|---|---|---|
| Unitarias | 2026-11-16 | 25 | 25 aprobadas, 0 fallidas |
| Verificación de tipos (`npm run typecheck`) | 2026-11-15 | — | Sin errores |
| Integración contra backend real | 2026-11-16 | 12 | 12 aprobadas, 0 fallidas |
| Manuales en emulador y teléfono | 2026-11-15 y 2026-11-16 | 14 | 14 aprobadas, 0 fallidas |

Entorno de la integración: backend del repositorio en `develop` (commit `e8c409d`) con perfiles `demo,dev` sobre una base limpia, PostgreSQL 17 y Mailpit con `docker compose`, Node.js 24.

## 1. Pruebas unitarias

| Archivo | Qué cubre |
|---|---|
| `src/utils/errores.test.ts` | Mensajes de error por pantalla: login (401 con orientación sobre la contraseña y el bloqueo), registro (duplicado que lleva a iniciar sesión, límite de intentos, datos no validados por Defensa, campos con formato incorrecto traducidos), red, tiempo de espera, 503, 5xx, 403 y 404 |
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

Cada ejecución registra a una persona del dataset de Defensa (por defecto `8000000005`); sobre una base ya usada hay que elegir otra con `PRUEBA_IDENTIFICACION` (`8000000005` a `8000000010`, la tabla completa está en la [guía de uso](../../docs/guia-de-uso.md)). El backend limita los registros a cinco intentos por IP cada 15 minutos y la prueba hace cuatro, así que dos ejecuciones seguidas dentro de ese plazo chocan con el límite: espera 15 minutos entre ejecuciones.

## 3. Pruebas manuales en emulador y teléfono

Todos los casos se ejecutaron con `EXPO_PUBLIC_API_MODO=real` contra el backend con perfiles `demo,dev`, y cada uno tiene su captura en [`evidencias/`](evidencias/):

- **2026-11-15, emulador Android** (dispositivo virtual `Medium_Phone_API_37.0`, Android 17, Expo Go con SDK 58): casos M-01 a M-09, M-13 y M-14. La pantalla se manejó por `adb`; las capturas son `.png`.
- **2026-11-16, teléfono físico con Expo Go**: casos con cámara M-10 a M-12, la solicitud de permiso de cámara (M-09c) y una consulta sin información (M-13b). Las capturas son `.jpeg` y llevan «telefono» en el nombre.

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
| M-09 | Escáner | Negar el permiso de cámara (dos veces) | Primero ofrece **Conceder permiso**; tras la denegación definitiva ofrece **Abrir ajustes**; el ingreso manual sigue disponible | OK | [a](evidencias/M-09a-solicitud-permiso.png), [b](evidencias/M-09b-permiso-denegado.png), [c (teléfono)](evidencias/M-09c-telefono-solicitud-permiso-camara.jpeg) |
| M-10 | Escáner | Escanear con la cámara un código de ciudadano requerido | Insignia **REQUERIDO** con la identificación leída (`9000000006`, del código con separadores `900.000.0006`) y la fecha de actualización | OK | [captura (teléfono)](evidencias/M-10-telefono-resultado-requerido.jpeg) |
| M-11 | Escáner | Escanear con la cámara el código `9000000001` | Insignia **NO REQUERIDO** | OK | [captura (teléfono)](evidencias/M-11-telefono-resultado-no-requerido.jpeg) |
| M-12 | Escáner | Escanear con la cámara el código `NO-ES-CEDULA` | La cámara detecta el código, no lo reconoce como identificación y muestra **No se pudo leer el código** con **Reintentar** | OK | [a (teléfono)](evidencias/M-12a-telefono-escaner-apuntando-al-codigo.jpeg), [b (teléfono)](evidencias/M-12b-telefono-codigo-ilegible-reintentar.jpeg) |
| M-13 | Consulta | Ingresar manualmente `9000000020` | Aviso de sin información, que aclara que no equivale a «No requerido» | OK | [captura](evidencias/M-13-manual-sin-informacion.png), [b (teléfono, identificación inexistente `9111111111`)](evidencias/M-13b-telefono-sin-informacion.jpeg) |
| M-14 | Red | Apagar el backend y consultar; encenderlo y tocar **Reintentar** | «No hay conexión con el servidor…» con **Reintentar**; al volver el backend la consulta responde | OK | [a](evidencias/M-14a-sin-conexion.png), [b](evidencias/M-14b-reintentar-con-servidor.png) |

Resultado: **14 de 14 casos aprobados**.

La cámara del emulador muestra una escena virtual y no permite enfocar un código de barras, por eso los casos con cámara (M-10 a M-12) se ejecutaron en un teléfono físico con los códigos de [codigos-prueba.md](codigos-prueba.md). En M-10 se usó el código con separadores (`900.000.0006`), que además comprueba que la app los limpia antes de consultar.

### Defectos encontrados y corregidos

| Defecto | Dónde | Corrección |
|---|---|---|
| Las peticiones al backend expiraban en Android 17 porque Expo Go no tenía el permiso de red local | Entorno del emulador | Conceder el permiso (documentado en el README) |
| Mensajes con falta de concordancia: «Los nombres es obligatorio», «Los apellidos es obligatorio» | `src/utils/validaciones.ts` | Ahora dicen «son obligatorios» y «admiten máximo» |
| Tras un registro aceptado, **Ir a iniciar sesión** apilaba un segundo Login con flecha de volver hacia la pantalla de registro aceptado | `src/screens/RegistroScreen.tsx` | Se usa `popTo('Login')` para regresar al Login existente |
| Al intentar registrar una cuenta que ya existía, el mensaje no decía qué hacer y la persona no lograba entrar | `src/utils/errores.ts`, `RegistroScreen.tsx` | El mensaje explica que ya tiene cuenta y ofrece **Ir a iniciar sesión** |
| El error de login no indicaba de dónde sale la contraseña ni que hay bloqueo temporal | `src/utils/errores.ts` | Ahora lo explica |
| «No se pudo leer el código» se veía en texto oscuro sobre fondo negro ([evidencia](evidencias/M-12b-telefono-codigo-ilegible-reintentar.jpeg)) | `src/screens/EscanerScreen.tsx` | Texto claro, con título y consejos; la instrucción sobre la cámara tiene fondo semitransparente |

Las correcciones de mensajes de error se cubren con `errores.test.ts` y con la prueba de integración; la del texto del escáner se comprueba visualmente en el teléfono.

Para comprobar que la contraseña no queda en el dispositivo (M-06): la app solo llama a `expo-secure-store` desde `src/storage/sesion.ts`, que guarda el token y su vencimiento; no usa AsyncStorage.
