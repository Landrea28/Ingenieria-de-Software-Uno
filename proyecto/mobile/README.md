# Sistema de Consulta Ciudadano — App móvil

App para el personal de la fuerza pública: se registra, inicia sesión y consulta el estado de antecedentes de un ciudadano escaneando el código de barras de su cédula. React Native + Expo, TypeScript. Android es el dispositivo de prueba; el mismo código corre en iOS. **La app no decide si alguien está requerido**: envía la identificación al backend y muestra tal cual el estado que este devuelve.

## Estado de implementación

| Módulo | Estado |
|---|---|
| Navegación (flujo de acceso y flujo principal) | Implementado |
| Registro de personal, con catálogo de fuerzas/rangos y estados pendiente/aceptado/rechazado | Implementado |
| Login, con «Recordar contraseña» y token en almacenamiento seguro | Implementado |
| Escáner de código de barras, permisos de cámara e ingreso manual alternativo | Implementado |
| Consulta de antecedentes y manejo de errores (sin información, red, tiempo de espera, servidor) | Implementado |
| Integración con el backend real (registro, correo, login, antecedentes, logout) | Verificada con `npm run test:api` (ver [docs/pruebas.md](docs/pruebas.md)) |
| Mockups, casos de uso, supuestos, pruebas y manual de usuario | Ver [docs/](docs/) |

## Requisitos

- Node.js 20 o posterior y npm.
- Expo Go en un dispositivo físico, o un emulador Android / simulador iOS configurado localmente.
- El [backend](../backend/README.md) corriendo, o el servidor simulado incluido en este proyecto (`npm run mock`) mientras el backend no esté disponible.

## Ejecutar

```bash
npm install
cp .env.example .env   # ajusta las URLs si no usas el emulador Android por defecto
npm start
```

Desde la terminal de Expo: `a` para abrir en el emulador Android, `i` para iOS, o escanea el QR con Expo Go en un dispositivo físico.

### Variables de entorno (`.env`)

| Variable | Para qué | Valor por defecto |
|---|---|---|
| `EXPO_PUBLIC_API_MODO` | `real` para hablar con el backend, `mock` para el servidor falso local | `real` |
| `EXPO_PUBLIC_API_URL` | URL del backend real | `http://10.0.2.2:8080` |
| `EXPO_PUBLIC_API_MOCK_URL` | URL del servidor falso (`npm run mock`) | `http://10.0.2.2:4010` |
| `EXPO_PUBLIC_API_TIMEOUT_MS` | Tiempo de espera de cada petición, en milisegundos | `10000` |

El emulador Android accede al backend del equipo anfitrión en `10.0.2.2`. En un dispositivo físico, reemplaza esa IP por la IP LAN del equipo donde corre el backend o el servidor falso. En modo `mock`, la interfaz siempre muestra un aviso visible para no confundirlo con el backend real.

### Si la app dice «El servidor tardó demasiado en responder» en Android reciente

Las versiones recientes de Android exigen el permiso de **red local** para que una app se conecte a direcciones privadas como `10.0.2.2` o la IP LAN del equipo (comprobado en el emulador con Android 17, API 37). Sin ese permiso las peticiones al backend expiran aunque el backend esté corriendo. En un teléfono, acepta el aviso de «dispositivos en la red local» de Expo Go (o actívalo en Ajustes → Apps → Expo Go → Permisos). En el emulador se puede conceder desde la terminal:

```bash
adb shell pm grant host.exp.exponent android.permission.ACCESS_LOCAL_NETWORK
```

## Servidor falso (`npm run mock`)

Simula el contrato OpenAPI del backend (catálogo, registro, login, antecedentes) para avanzar sin depender de que el backend esté corriendo. **No es el backend real**: todo vive en memoria y se reinicia al reiniciar el proceso.

```bash
npm run mock          # puerto 4010; cambiar con MOCK_PORT
```

Casos de prueba incluidos (ver cabecera de [mock-server/servidor.js](mock-server/servidor.js) para el detalle completo):

| Caso | Identificación de prueba | Resultado |
|---|---|---|
| Login válido | `8000000001`–`8000000004`, contraseña `Ab123c` | `200` con token |
| Registro aceptado (primera vez) | `8000000001`–`8000000004` | `201` |
| Registro con integración pendiente | `8000000503` | `503 INTEGRACION_PENDIENTE` |
| Registro fuera de la validación de Defensa | cualquier otra identificación | `400` |
| Antecedente conocido | `9000000001`–`9000000019` | `200` con `REQUERIDO`/`NO_REQUERIDO` |
| Sin información | `9000000020` o cualquier identificación no registrada | `404 SIN_INFORMACION` |
| Error del servidor | `9000000500` | `500` |
| Tiempo de espera agotado | `9000000600` | tarda 15 s (para probar el timeout del cliente) |

Las identificaciones de personal de esta tabla son propias del servidor falso. Con el backend real y el perfil `demo`, las identidades SIMULADAS de Defensa están en `backend/src/main/resources/db/demo/V101__defensa_personal_ficticio.sql` y la contraseña llega a Mailpit (`http://localhost:8025`).

## Pruebas

```bash
npm test        # pruebas unitarias (node --test) sobre validaciones, tiempo de sesión y extracción del código de barras
npm run typecheck
```

Las pruebas unitarias cubren lógica pura (validaciones de formulario, cálculo de expiración de sesión, extracción de la identificación desde el código de barras). No hay pruebas de componentes de React Native en este proyecto; la interacción de pantallas se verifica manualmente en el emulador o dispositivo.

### Prueba de integración contra el backend real

```bash
npm run test:api
```

Ejecuta el flujo completo con el mismo cliente HTTP y los mismos servicios que usan las pantallas (`src/api`), contra el backend real: catálogo, registro validado contra Defensa (SIMULADA), contraseña de 6 caracteres recibida en Mailpit, login con y sin «Recordar contraseña», consulta de antecedentes (requerido, no requerido, sin información) y cierre de sesión. No usa el servidor falso.

Requiere el [backend](../backend/README.md) con los perfiles `demo,dev` sobre una base limpia y Mailpit (`docker compose up -d`). Variables opcionales: `EXPO_PUBLIC_API_URL` (por defecto `http://127.0.0.1:8080`) y `MAILPIT_URL` (por defecto `http://127.0.0.1:8025`).

Los casos, el resultado de la última ejecución y la lista de pruebas manuales en dispositivo están en [docs/pruebas.md](docs/pruebas.md). Los códigos de barras para probar el escáner están en [docs/codigos-prueba.md](docs/codigos-prueba.md).

## Escáner de código de barras

Usa `expo-camera`. Ver [docs/supuestos.md](docs/supuestos.md) (supuesto S-10) para el formato de identificación asumido y cómo se validó con códigos de prueba ficticios, ya que no se contó con una cédula física. Si la lectura automática no resulta confiable con un código real, la pantalla de escaneo siempre ofrece el ingreso manual de la identificación como alternativa.

## Estructura

```
src/
├── api/          # cliente HTTP, servicios por endpoint y traducción de errores a mensajes
├── components/   # Botón, CampoTexto, Selector, Pantalla, estados de carga/error reutilizables
├── config/       # variables de entorno
├── context/      # AuthContext: sesión, token y expiración
├── navigation/   # stacks de acceso y principal
├── screens/      # Login, Registro, Inicio, Escáner, Resultado
├── storage/      # SecureStore para el token de sesión
├── types/        # tipos del contrato HTTP
└── utils/        # validaciones, tiempo de expiración, extracción del código de barras
docs/
├── mockups.md y mockups/                  # bocetos de las cinco pantallas
├── casos-de-uso.md                        # diagrama y especificación de casos de uso
├── supuestos.md                           # decisiones y supuestos (S-01 a S-10)
├── pruebas.md                             # casos de prueba y resultados
├── codigos-prueba.md y codigos-prueba/    # códigos de barras ficticios para el escáner
└── manual-usuario.md                      # guía para el personal que usa la app
mock-server/      # servidor falso para desarrollar sin depender del backend
pruebas-api/      # prueba de integración contra el backend real
```

No subir contraseñas, tokens, archivos `.env` reales ni datos personales reales al repositorio.
