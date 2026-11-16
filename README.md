# Sistema de Consulta Ciudadano

Monorepo académico con una API REST, aplicación web administrativa y app móvil
para consultar datos de antecedentes **ficticios**. No usar con información real
ni exponer en Internet. Las validaciones de Defensa y el correo son simulados.

## Requisitos

- Git.
- JDK 21.
- Docker y Docker Compose para PostgreSQL y Mailpit.
- Node.js y npm para la app móvil.
- Bash para `smoke-test.sh`; Windows PowerShell para `smoke-test.ps1`.

Backend y web incluyen Maven Wrapper (`./mvnw`), no es necesario instalar Maven.
En Windows use `mvnw.cmd`.

## Configuración

1. En `proyecto/backend`, copie `.env.example` a `.env`.
2. Defina valores locales para `DB_PASSWORD`, `JWT_SECRET` (aleatorio, al menos
   32 caracteres), `ADMIN_INITIAL_IDENTIFICACION`, `ADMIN_INITIAL_EMAIL`,
   `ADMIN_INITIAL_PASSWORD` y las variables `MAIL_*` si difieren de Mailpit.
3. No incluya `.env`, contraseñas, tokens ni datos personales reales en commits.

## Orden de arranque

Mantenga los servicios en ejecución en terminales separadas.

### 1. PostgreSQL y Mailpit

```bash
cd proyecto/backend
docker compose up -d
```

Mailpit queda en <http://localhost:8025>; SMTP en `localhost:1025`.

### 2. Backend

Desde otra terminal, cargue las variables del archivo local y arranque el
backend con los perfiles `demo,dev` para instalar datos ficticios y habilitar
Swagger solo en desarrollo:

```bash
cd proyecto/backend
set -a
. ./.env
set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo,dev
```

La API escucha en <http://localhost:8080>; health:
<http://localhost:8080/api/health>. `demo` solo debe usarse con una base limpia.
No arranque un perfil demo sobre datos que quiera conservar.

### 3. Web

```bash
cd proyecto/web
API_BASE_URL=http://localhost:8080 ./mvnw spring-boot:run
```

Abra <http://localhost:8081/login> e ingrese con el usuario administrador
configurado mediante `ADMIN_INITIAL_*`.

### 4. Móvil

```bash
cd proyecto/mobile
npm ci
npm start
```

La configuración está en `proyecto/mobile/.env.example`. Para un emulador
Android, la URL local del host suele ser `http://10.0.2.2:8080`; en un teléfono,
configure la IP accesible del equipo que ejecuta el backend. Use modo `real`
para consumir la API; el modo `mock` es una simulación claramente etiquetada.

## Pruebas y verificaciones

```bash
cd proyecto/backend
./mvnw clean verify

cd ../web
./mvnw clean verify

cd ../mobile
npm ci
npm test
npm run typecheck
```

Para una prueba de humo contra la API real, arranque backend, PostgreSQL y
Mailpit; defina `SMOKE_ADMIN_IDENTIFICACION` y `SMOKE_ADMIN_PASSWORD` solo en la
sesión de terminal y ejecute desde la raíz:

```bash
bash proyecto/scripts/smoke-test.sh
```

En Windows, defina las mismas variables de entorno y ejecute
`proyecto/scripts/smoke-test.ps1` desde PowerShell. La ejecución Windows aún
está pendiente; la salida Bash guardada está en
[proyecto/backend/docs/evidencias/smoke-test-salida.txt](proyecto/backend/docs/evidencias/smoke-test-salida.txt).

## Estructura y documentación

| Ruta | Contenido |
|---|---|
| [proyecto/backend/](proyecto/backend/) | API, migraciones Flyway, contrato OpenAPI y documentación |
| [proyecto/web/](proyecto/web/) | Cliente web administrativo Spring MVC/Thymeleaf |
| [proyecto/mobile/](proyecto/mobile/) | Cliente React Native/Expo y pruebas manuales de dispositivo |
| [proyecto/docs/](proyecto/docs/) | Checklist final y plantillas pendientes del equipo |
| [proyecto/backend/docs/pruebas/](proyecto/backend/docs/pruebas/) | Plan de pruebas y resultados medidos de Surefire |

Las pruebas manuales móviles y las pendientes de cámara están descritas en
[proyecto/mobile/docs/pruebas.md](proyecto/mobile/docs/pruebas.md). Defensa,
correo y datos semilla se mantienen simulados; consulte los supuestos del
[backend](proyecto/backend/docs/supuestos.md).
