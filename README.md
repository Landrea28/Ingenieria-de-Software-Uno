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

En Android reciente, Expo Go necesita el permiso de red local para alcanzar el
backend; si la app dice que el servidor tardó demasiado en responder, vea la
sección correspondiente del [README del móvil](proyecto/mobile/README.md).
Las pruebas manuales móviles y los casos de cámara pendientes están en
[proyecto/mobile/docs/pruebas.md](proyecto/mobile/docs/pruebas.md).

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

## Estructura del repositorio

```
proyecto/
├── backend/   # API REST: Java 21, Spring Boot (Spring MVC, Tomcat embebido), PostgreSQL, Hibernate/JPA, Flyway
├── web/       # aplicación web administrativa: Spring MVC + Thymeleaf, consume la API
├── mobile/    # app móvil: React Native + Expo (Android; el mismo código corre en iOS)
├── scripts/   # prueba de humo contra la API real (Bash y PowerShell)
└── docs/      # registro de supuestos y checklist final de la entrega
```

La web y la app móvil son clientes del mismo backend; ninguna accede directamente
a la base de datos.

## Equipo y módulos

| Integrante | Módulo |
|---|---|
| Sebastián | Backend, arquitectura y API |
| Lina | Aplicación web administrativa |
| Mariana | Aplicación móvil |
| Julián | Seguridad, autenticación y autorización |
| David | Modelo de datos, ORM, consultas y PDF |

## Documentación

| Tema | Dónde está |
|---|---|
| Instalación y uso de cada módulo | [backend](proyecto/backend/README.md) · [web](proyecto/web/README.md) · [móvil](proyecto/mobile/README.md) |
| Requisitos funcionales y no funcionales | [requisitos.md](proyecto/backend/docs/requisitos.md) · [RNF de seguridad](proyecto/backend/docs/requisitos/rnf-seguridad.md) |
| Arquitectura y clases | [arquitectura.md](proyecto/backend/docs/arquitectura.md) · [especificacion-clases.md](proyecto/backend/docs/especificacion-clases.md) |
| Contrato de la API | `proyecto/backend/src/main/resources/static/openapi/` (Swagger en `/swagger-ui.html` con el perfil `dev`) |
| Modelo de datos | [modelo ER](proyecto/backend/docs/modelo-datos/modelo-er.md) · [modelo conceptual](proyecto/backend/docs/modelo-datos/modelo-conceptual.md) · [diccionario](proyecto/backend/docs/modelo-datos/diccionario-datos.md) · [reconstrucción de la base](proyecto/backend/docs/modelo-datos/reconstruccion-base.md) |
| Seguridad | [decisiones](proyecto/backend/docs/decisiones/seguridad.md) · [matriz rol-permiso](proyecto/backend/docs/decisiones/matriz-rol-permiso.md) |
| Casos de uso del móvil y prototipos | [casos-de-uso.md](proyecto/mobile/docs/casos-de-uso.md) · [mockups.md](proyecto/mobile/docs/mockups.md) |
| Manuales de usuario | [web](proyecto/web/docs/manual-usuario.md) · [móvil](proyecto/mobile/docs/manual-usuario.md) |
| Pruebas y resultados | [backend](proyecto/backend/docs/pruebas/plan-de-pruebas.md) · [seguridad](proyecto/backend/docs/pruebas/resultados-seguridad.md) · [web](proyecto/web/docs/pruebas.md) · [móvil](proyecto/mobile/docs/pruebas.md) |
| Supuestos y decisiones | [supuestos.md](proyecto/docs/supuestos.md): registro único de simulaciones, datos y API, seguridad y app móvil |
| Estado de la entrega | [checklist-final.md](proyecto/docs/checklist-final.md) |

La planeación con Scrum (backlog, estimación, sprints, burndown y actas), la
especificación de casos de uso y la trazabilidad se entregan en el libro
`00_Consolidado_Entrega.xlsx`, fuera del repositorio.

## Estado del alcance

| Historia | Estado |
|---|---|
| HU01 a HU03: registro, validación y login del personal | Terminadas (móvil y backend) |
| HU04 a HU06: registro, consulta y actualización de ciudadanos | Terminadas (web y backend) |
| HU07: escaneo de cédula y consulta de antecedentes | Terminada (móvil y backend) |
| HU08 y HU09: usuarios, perfiles y permisos | Parcial: disponibles como API; sin pantalla web |
| HU10: reporte PDF por estado | Terminada (web y backend) |

Defensa, el correo y los datos semilla son **simulados**; los supuestos explican
por qué y cómo se reemplazarían por integraciones reales.

## Ramas

- `main`: versión estable y entregable.
- `develop`: integración del equipo.
- `feature/<módulo>`: trabajo de cada módulo (`backend`, `web`, `movil`, `seguridad`, `datos`), que se integra a `develop`.

No se suben archivos `.env`, contraseñas, tokens ni datos personales reales.
