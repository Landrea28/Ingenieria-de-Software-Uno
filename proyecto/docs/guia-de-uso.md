# Guía de uso

Paso a paso para **correr el proyecto, abrir el correo, registrarse e iniciar sesión**, en la web y en la app móvil. Si algo falla, la sección [Qué hacer cuando algo falla](#qué-hacer-cuando-algo-falla) explica cada mensaje.

> Todo es de **prueba**: los datos son ficticios, la validación del Departamento de Defensa es simulada y el correo no sale a internet. No uses el proyecto con información real ni lo expongas en una red compartida.

## Qué vas a tener corriendo

| Pieza | Para qué sirve | Dirección |
|---|---|---|
| PostgreSQL | Base de datos | `localhost:5432` |
| Mailpit | «Bandeja de entrada» de pruebas: aquí llega la contraseña | <http://localhost:8025> |
| Backend | API REST con toda la lógica y la seguridad | <http://localhost:8080> |
| Web | Pantallas del administrador | <http://localhost:8081/login> |
| App móvil | La usa el personal de la fuerza pública (Expo Go en el teléfono o un emulador) | Expo, puerto `8081` del computador |

La web y la app son clientes del mismo backend; ninguna toca la base de datos.

## 1. Requisitos

- Git, JDK 21 o superior, Docker con Docker Compose y Node.js con npm.
- Para la app: la aplicación **Expo Go** en el teléfono, o un emulador Android.
- No necesitas instalar Maven: cada módulo trae `./mvnw`.

## 2. Configurar las variables (una sola vez)

En `proyecto/backend`, copia el archivo de ejemplo:

```bash
cp .env.example .env
```

Edita `.env` y completa estos valores (no los subas a Git: `.env` está ignorado):

| Variable | Qué poner |
|---|---|
| `DB_PASSWORD` | La contraseña que quieras para la base de datos local |
| `JWT_SECRET` | Un texto aleatorio de al menos 32 caracteres. Para generarlo: `openssl rand -hex 32` |
| `ADMIN_INITIAL_IDENTIFICACION` | La identificación del administrador: solo números, de 6 a 10 dígitos (por ejemplo `7000000001`) |
| `ADMIN_INITIAL_EMAIL` | Cualquier correo, por ejemplo `admin@example.test` |
| `ADMIN_INITIAL_PASSWORD` | **La contraseña del administrador la eliges tú aquí.** Será la que uses en la web |

Las demás variables ya traen un valor que funciona.

## 3. Levantar la base de datos y el correo

```bash
cd proyecto/backend
docker compose up -d
```

`docker compose` lee la contraseña de `.env`. Se crean dos contenedores: PostgreSQL y Mailpit.

## 4. Levantar el backend

El backend no lee `.env` por sí solo: hay que cargar las variables en la terminal y arrancarlo con los perfiles `demo` y `dev`.

```bash
cd proyecto/backend
set -a
. ./.env
set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo,dev
```

- `demo` carga los datos ficticios (ciudadanos y personal de Defensa). **Úsalo solo sobre una base limpia.**
- `dev` activa Swagger en <http://localhost:8080/swagger-ui.html>.

Cuando veas `Started ConsultaCiudadanoApplication`, comprueba que responde:

```bash
curl http://localhost:8080/api/health
```

Debe contestar `{"estado":"OK"}`. Deja esa terminal abierta.

## 5. Abrir el correo (Mailpit)

La contraseña de cada persona que se registra **no se muestra en la app**: el sistema la genera y la «envía por correo». En este entorno de pruebas ese correo lo recibe Mailpit.

1. Abre <http://localhost:8025> en el navegador del computador donde corre Docker.
2. Verás una lista de mensajes. Cada uno tiene como destinatario el correo que se escribió al registrarse.
3. Abre el mensaje cuyo asunto es **`[SIMULADO] Credenciales de acceso - Sistema de Consulta Ciudadano`**.
4. En el texto aparece: `Su contraseña temporal es: XXXXXX`. Esa es la contraseña: **6 caracteres con mayúsculas y minúsculas** que debes escribir tal cual.

Dos cosas que conviene saber:

- Mailpit no guarda los correos de forma permanente: **si reinicias el contenedor, se pierden**. No existe «recuperar contraseña». Si te pasa, usa otra identidad de la tabla de abajo o reinicia la base (ver [Empezar de cero](#empezar-de-cero)).
- Si la lista está vacía, es que nadie se ha registrado todavía o que el registro fue rechazado.

## 6. Registrarse en la app móvil

El registro **solo acepta a personas que estén en el dataset simulado de Defensa**. Los datos del formulario deben coincidir con una fila de esta tabla (los nombres no distinguen mayúsculas; el **ID de fuerza** sí debe ir exacto):

| Identificación | Nombres | Apellidos | Fuerza pública | Rango | ID de fuerza |
|---|---|---|---|---|---|
| `8000000001` | Usuario | Prueba1 | Policia Nacional | Patrullero | `DEMO-001` |
| `8000000002` | Usuario | Prueba2 | Ejercito Nacional | Soldado | `DEMO-002` |
| `8000000003` | Usuario | Prueba3 | Armada Nacional | Marinero | `DEMO-003` |
| `8000000004` | Usuario | Prueba4 | Fuerza Aerea Colombiana | Aerotecnico | `DEMO-004` |
| `8000000005` | Ana | Prueba5 | Policia Nacional | Subintendente | `DEMO-005` |
| `8000000006` | Luis | Prueba6 | Ejercito Nacional | Cabo | `DEMO-006` |
| `8000000007` | Mia | Prueba7 | Armada Nacional | Cabo Primero | `DEMO-007` |
| `8000000008` | Juan | Prueba8 | Fuerza Aerea Colombiana | Tecnico Primero | `DEMO-008` |
| `8000000009` | Eva | Prueba9 | Policia Nacional | Intendente | `DEMO-009` |
| `8000000010` | Leo | Prueba10 | Ejercito Nacional | Sargento | `DEMO-010` |

Pasos:

1. En la pantalla de inicio de sesión toca **Registrarme**.
2. Llena el formulario con **una fila completa** de la tabla. En **Correo electrónico** escribe cualquier dirección con formato válido, por ejemplo `ana.prueba5@example.test`.
3. Toca **Registrarme**. Si todo coincide, la app muestra **Registro aceptado**.
4. Abre Mailpit (sección 5) y copia la contraseña.
5. Toca **Ir a iniciar sesión**.

Reglas:

- Cada identificación se registra **una sola vez**, y cada correo también: si repites cualquiera de las dos, la app dice que ya tienes una cuenta (y ofrece ir a iniciar sesión).
- Se permiten **5 intentos de registro cada 15 minutos** desde la misma dirección IP.
- Si un dato no coincide con la tabla, la app responde «No pudimos validar tus datos con el Departamento de Defensa» sin decir cuál falló: revisa fuerza, rango, ID de fuerza, nombres y apellidos.

## 7. Iniciar sesión en la app móvil

1. Escribe tu **identificación** (por ejemplo `8000000005`).
2. Escribe la **contraseña de 6 caracteres** que llegó a Mailpit.
3. Activa **Recordar contraseña** si quieres que la sesión quede guardada al cerrar la app. Se guarda solo un token cifrado, nunca la contraseña.
4. Toca **Iniciar sesión**.

| | Sin «Recordar contraseña» | Con «Recordar contraseña» |
|---|---|---|
| Duración de la sesión | 15 minutos | 8 horas |
| Al cerrar y abrir la app | Pide iniciar sesión | Entra directo mientras el token valga |

Si fallas la contraseña **6 veces seguidas**, esa identificación se bloquea **15 minutos**. La app no distingue el bloqueo de una contraseña incorrecta (es intencional, para no revelar qué cuentas existen): si estás seguro de la contraseña y no entra, espera 15 minutos.

Con la sesión iniciada: **Escanear cédula** abre la cámara, lee el código de barras y muestra **REQUERIDO**, **NO REQUERIDO** o el aviso de que no hay información. Si no puedes escanear, usa **Ingresar identificación manualmente**.

### Datos para consultar

El perfil `demo` carga 20 ciudadanos ficticios (`9000000001` a `9000000020`):

| Identificación | Resultado |
|---|---|
| `9000000003`, `9000000006`, `9000000009`, `9000000012`, `9000000015`, `9000000018` | REQUERIDO |
| Los demás de `9000000001` a `9000000019` (por ejemplo `9000000001`) | NO REQUERIDO |
| `9000000020` y cualquier otra identificación | «Sin información» (no equivale a «No requerido») |

Los códigos de barras listos para escanear están en [mobile/docs/codigos-prueba.md](../mobile/docs/codigos-prueba.md).

## 8. Iniciar sesión en la web (administrador)

```bash
cd proyecto/web
./mvnw spring-boot:run
```

Abre <http://localhost:8081/login> e ingresa con la identificación y la contraseña que pusiste en `ADMIN_INITIAL_IDENTIFICACION` y `ADMIN_INITIAL_PASSWORD` del `.env`.

El personal de la fuerza pública **no** puede entrar a la web: solo el perfil administrador. Desde la web se registran, consultan y editan ciudadanos y se descarga el reporte PDF (ver el [manual de usuario web](../web/docs/manual-usuario.md)).

## 9. Probar en un teléfono con Expo Go

El teléfono y el computador deben estar **en la misma red wifi**.

1. Averigua la IP del computador en esa red:

   ```bash
   hostname -I
   ```

   Usa la que empieza por `192.168.` o `10.` (por ejemplo `192.168.1.5`).

2. Reinicia el backend para que acepte conexiones de la red, no solo del propio computador (hazlo solo en una red de confianza):

   ```bash
   cd proyecto/backend
   set -a; . ./.env; set +a
   SERVER_ADDRESS=0.0.0.0 ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo,dev
   ```

3. Si el computador tiene firewall, abre los puertos `8080` (backend) y `8081` (Expo). En Fedora: `sudo firewall-cmd --add-port=8080/tcp --add-port=8081/tcp`.

4. Arranca Expo diciéndole dónde está el backend (cambia la IP por la tuya):

   ```bash
   cd proyecto/mobile
   npm ci
   EXPO_PUBLIC_API_URL=http://192.168.1.5:8080 npx expo start
   ```

5. En el teléfono abre **Expo Go** y escanea el código QR que aparece en la terminal (en iPhone, con la cámara; en Android, desde Expo Go). También puedes escribir a mano `exp://192.168.1.5:8081`.

6. **Acepta el permiso de red local** cuando el teléfono lo pida; sin él la app no alcanza el backend y verás «El servidor tardó demasiado en responder». En Android también está en Ajustes → Apps → Expo Go → Permisos.

7. Cuando entres a **Escanear cédula**, acepta el permiso de cámara.

> En lugar de escribir la variable en cada arranque puedes copiar `proyecto/mobile/.env.example` a `.env` y poner ahí `EXPO_PUBLIC_API_URL`. Si cambias el `.env`, reinicia Expo.

## 10. Probar en el emulador de Android

Con el emulador encendido, en `proyecto/mobile`:

```bash
npx expo start --android
```

El emulador ve el computador en `10.0.2.2`, así que el `.env` de ejemplo ya funciona (`EXPO_PUBLIC_API_URL=http://10.0.2.2:8080`). Si la app dice que el servidor tardó demasiado, concede el permiso de red local:

```bash
adb shell pm grant host.exp.exponent android.permission.ACCESS_LOCAL_NETWORK
```

La cámara del emulador es una escena virtual y no sirve para leer códigos de barras: para escanear usa un teléfono físico.

## Qué hacer cuando algo falla

Mensajes que muestra la app y qué significan:

| Mensaje | Causa probable | Qué hacer |
|---|---|---|
| **Identificación o contraseña incorrectas** | Contraseña mal escrita, o identificación sin registrar | Usa la contraseña de 6 caracteres del correo en Mailpit, respetando mayúsculas. Si la identificación nunca se registró, toca **Registrarme** |
| Lo anterior, pero estás seguro de la contraseña | La identificación se bloqueó por 6 intentos fallidos | Espera 15 minutos |
| **Esta identificación o este correo ya tienen una cuenta** | Ya te registraste antes, o el correo lo usó otra persona | No te registres de nuevo: toca **Ir a iniciar sesión**. Si perdiste la contraseña, usa otra identidad de la tabla (con otro correo) o reinicia la base |
| **No pudimos validar tus datos con el Departamento de Defensa** | Los datos no coinciden con la tabla de la sección 6 | Copia la fila completa de la tabla, incluido el ID de fuerza |
| **Hay datos con un formato incorrecto** | Algún campo no cumple el formato (la app lista cuáles) | Corrige los campos indicados |
| **Hiciste demasiados intentos de registro** | Más de 5 intentos en 15 minutos desde la misma IP | Espera 15 minutos |
| **No hay conexión con el servidor** | El backend está apagado, o el teléfono no está en la misma red, o la IP del `.env` es incorrecta | Comprueba `curl http://<IP>:8080/api/health` desde otro dispositivo de la red y revisa la sección 9 |
| **El servidor tardó demasiado en responder** | Casi siempre falta el permiso de red local o el firewall bloquea el puerto | Concede el permiso de red local (sección 9) y abre el puerto `8080` |
| **Tu sesión venció** | El token caducó (15 minutos, u 8 si usaste «Recordar contraseña») | Inicia sesión de nuevo |
| **No se pudo leer el código** | Poca luz, reflejos, o el código no es una cédula | Mejora la luz, mantén el código dentro del marco, o usa **Ingresar identificación manualmente** |
| **No hay información de antecedentes** | La identificación no existe o no tiene estado | Es una respuesta válida: no significa que la persona no esté requerida |
| **El servidor tuvo un problema** | Error interno del backend | Revisa la terminal del backend; reintenta |

Otros problemas frecuentes:

- **Mailpit sin mensajes:** el registro no llegó a aceptarse, o Mailpit se reinició. Revisa que la app mostró «Registro aceptado».
- **El backend no arranca:** revisa que `JWT_SECRET` tenga al menos 32 caracteres, que PostgreSQL esté arriba (`docker ps`) y que hayas cargado el `.env` (`set -a; . ./.env; set +a`).
- **En la web, «acceso denegado»:** esa cuenta no es de administrador. Usa la del `.env`.
- **En el backend, «Admin inicial omitido»:** faltan `ADMIN_INITIAL_*` en el `.env`; sin ellas no existe administrador.

## Empezar de cero

Para apagar sin perder datos:

```bash
cd proyecto/backend
docker compose down
```

Para **borrar la base y los correos** (se pierden las cuentas registradas) y volver a cargar los datos demo:

```bash
docker compose down -v
docker compose up -d
```

y vuelve a arrancar el backend con `demo,dev` (sección 4).

## Comprobar que todo funciona

| Qué | Cómo |
|---|---|
| Backend vivo | `curl http://localhost:8080/api/health` → `{"estado":"OK"}` |
| Prueba de humo de la API | `bash proyecto/scripts/smoke-test.sh` (ver el [README raíz](../../README.md)) |
| Rendimiento | [backend/docs/pruebas/rendimiento.md](../backend/docs/pruebas/rendimiento.md) |
| Pruebas del backend | `cd proyecto/backend && ./mvnw clean verify` |
| Pruebas de la web | `cd proyecto/web && ./mvnw clean verify` |
| Pruebas de la app | `cd proyecto/mobile && npm test && npm run typecheck` |
| App contra el backend real | `cd proyecto/mobile && npm run test:api` ([detalles](../mobile/docs/pruebas.md)) |
