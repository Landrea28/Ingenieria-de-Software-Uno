# Sistema de Consulta Ciudadano — Web administrativa

Aplicación del **administrador**: inicia sesión, consulta, registra y edita ciudadanos, y solicita reportes en PDF.
Está hecha con Spring MVC + Thymeleaf (vistas del lado del servidor) y **consume la API REST de [`../backend`](../backend)**;
no accede directamente a la base de datos. La lógica de negocio y la seguridad están en el backend.

| Documento | Para qué |
|---|---|
| [docs/manual-usuario.md](docs/manual-usuario.md) | Manual breve para el administrador |
| [docs/pruebas.md](docs/pruebas.md) | Casos de prueba, resultados y defectos corregidos |
| [docs/demo.md](docs/demo.md) | Guion de la demo, plan B y lista de verificación para la integración |

## Estado de implementación

| Funcionalidad | Estado |
|---|---|
| Login y cierre de sesión del administrador | Conectado a la API |
| Listado paginado y filtro de ciudadanos | Conectado a la API |
| Consulta por identificación y detalle | Conectado a la API |
| Registro y edición (10 campos) con validación | Conectado a la API |
| Reporte PDF por estado (Requerido / No requerido) | Conectado a la API (el PDF lo genera el backend) |

## Requisitos

- JDK 21 o superior (`java -version`). No hace falta instalar Maven: se usa el wrapper incluido (`mvnw`).
- Para usar datos reales: el backend en ejecución (ver [`../backend/README.md`](../backend/README.md)).
  El backend usa Lombok y **necesita un JDK 21 completo** para compilar (con JDK más nuevos falla con `EndPosTable`).

## Ejecutar

```bash
cd proyecto/web
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Abrir <http://localhost:8081/login> e ingresar con un usuario **ADMINISTRADOR** del backend
(por ejemplo, el admin inicial que el backend crea con `ADMIN_INITIAL_*`). Con el perfil `demo` del backend
se cargan ciudadanos ficticios (`9000000001` a `9000000019`).

**Sin backend (demo o desarrollo):**

```bash
SPRING_PROFILES_ACTIVE=mock ./mvnw spring-boot:run
```

Con el perfil `mock` el login acepta cualquier credencial, los datos son ficticios (`1000000001` a `1000000003`) y
todas las pantallas muestran **DATOS SIMULADOS — PRUEBA**. El reporte "Requerido" trae un PDF simulado y
"No requerido" sale vacío, para mostrar el mensaje de sin resultados.

### Configuración

| Variable | Por defecto | Descripción |
|---|---|---|
| `API_BASE_URL` | `http://localhost:8080` | URL base de la API REST |
| `SERVER_PORT` | `8081` | Puerto de la web (el backend usa el 8080) |
| `SPRING_PROFILES_ACTIVE` | `api` | `api` = backend real; `mock` = datos simulados sin backend |

`.env.example` muestra los valores. **No subas archivos `.env` con valores reales ni credenciales**; están en `.gitignore`.

## Pruebas

```bash
./mvnw test                         # 83 pruebas automáticas, no necesitan backend
```

Prueba de humo contra un entorno en ejecución (integración o antes de sustentar). Las credenciales van en variables,
nunca en el script:

```bash
ADMIN_IDENTIFICACION=... ADMIN_CONTRASENA='...' ./scripts/prueba-humo.sh
# Opcional: AGENTE_IDENTIFICACION / AGENTE_CONTRASENA (rol incorrecto), ESCRITURA=1 (registra y edita), WEB_URL
```

Detalle de casos y resultados en [docs/pruebas.md](docs/pruebas.md).

## Pantallas y endpoints

| Ruta web | Pantalla | API |
|---|---|---|
| `/login`, `/logout` | Ingreso y cierre de sesión | `POST /api/auth/login`, `POST /api/auth/logout` |
| `/` | Panel principal (total de ciudadanos) | `GET /api/ciudadanos` |
| `/ciudadanos?busqueda=&pagina=` | Listado con filtro parcial, 20 por página | `GET /api/ciudadanos` |
| `/ciudadanos/consulta?identificacion=` | Consulta exacta (acepta puntos y espacios) → detalle | — |
| `/ciudadanos/{identificacion}` | Detalle | `GET /api/ciudadanos/{identificacion}` |
| `/ciudadanos/nuevo` | Registrar | `POST /api/ciudadanos` |
| `/ciudadanos/{identificacion}/editar` | Editar (precargado) | `GET` y `PUT /api/ciudadanos/{identificacion}` |
| `/reportes` | Filtro Requerido / No requerido | — |
| `/reportes/pdf?estado=&modo=ver\|descargar` | PDF en el navegador o descargado | `GET /api/reportes/ciudadanos?estado=` |

Las rutas con `{identificacion}` solo aceptan 6 a 10 dígitos; otra ruta da 404 con la página `error.html`.
El navegador solo envía GET y POST: al editar, el formulario hace POST a la web y la web hace el `PUT` a la API.

## Cómo funciona

- **Sesión:** el token JWT se guarda en la sesión del servidor web (`SesionAdmin`), nunca en el navegador. El navegador
  solo tiene la cookie `JSESSIONID` (`HttpOnly`, `SameSite=Lax`, sin `;jsessionid` en las URL). La sesión web dura
  lo mismo que el token: 15 min, u 8 h con "Recordar contraseña".
- **Solo administradores:** la respuesta del login no trae el rol; tras el login se hace una consulta que exige
  `ADMINISTRADOR`. Si la API responde 403, se cierra la sesión y se informa que el usuario no tiene permisos.
- **Acceso:** `SesionInterceptor` envía al login a quien no tenga sesión. Si la API responde 401/403 en cualquier
  pantalla (token vencido, revocado o sin rol), `ManejoErroresApi` cierra la sesión y vuelve al login con un mensaje.
- **Errores de la API:** `TraductorErroresApi` convierte cada respuesta en una excepción con nombre: 400 validación
  (los detalles "campo: mensaje" se muestran en su campo), 404 no encontrado / sin resultados, 409 duplicado,
  5xx, red o tiempo agotado (3 s conexión, 10 s lectura) → "No fue posible conectar con el servidor".
- **Validación en dos capas** con las reglas de `form/ReglasCiudadano.java` (copiadas de `CiudadanoRequest` del backend):
  en el navegador (`static/js/validacion.js`, mensajes en español junto a cada campo) y en el servidor web
  (`CiudadanoForm` con Bean Validation), por si el navegador no ejecuta JavaScript. El backend vuelve a validar todo.
- **Cabeceras de seguridad** (`CabecerasSeguridadFilter`): `Cache-Control: no-store` en las pantallas (tras cerrar sesión,
  "Atrás" no muestra datos), `X-Frame-Options: DENY` y CSP `frame-ancestors 'none'` (no se puede incrustar en otro sitio),
  `default-src 'self'`, `nosniff` y `Referrer-Policy: same-origin`.
- **Estados de la interfaz:** carga (barra de progreso, `js/estados.js`), vacío ("Aún no hay ciudadanos…" o
  "No hay resultados para…"), éxito (mensaje verde, fila resaltada) y error (mensaje con **Reintentar**; los formularios
  conservan lo escrito).

### Reglas de los campos

| Campo | Regla |
|---|---|
| identificacion | obligatorio, 6 a 10 dígitos; no se puede cambiar al editar |
| nombres, apellidos | obligatorio, máx. 100, solo letras y espacios |
| fechaNacimiento | obligatorio, no futura |
| fechaExpedicion | obligatorio, no futura, posterior a la fecha de nacimiento |
| lugarNacimiento, lugarExpedicion | obligatorio, máx. 150 |
| rh | `+` o `-` (lista) |
| grupoSanguineo | A, B, AB u O (lista) |
| estatura | 0.50 a 2.50 m, máx. 2 decimales |

## Estructura

```
src/main/java/co/edu/konrad/consultaciudadano/web/
├── config/      ApiRestConfig (RestClient), TraductorErroresApi, CabecerasSeguridadFilter, WebConfig, ApiProperties
├── controller/  Login, Panel, Ciudadano, Reporte + ModeloGlobal y ManejoErroresApi (401/403/red)
├── dto/         CiudadanoDto, PaginaCiudadanos, LoginResponse, ErrorApi, EstadoAntecedente (contrato OpenAPI)
├── form/        CiudadanoForm y ReglasCiudadano
├── service/     Clientes de la API (interfaz + implementación Rest para "api" y Mock para "mock"),
│                AccesoAdminService y excepciones Api*
└── sesion/      SesionAdmin (token en la sesión) y SesionInterceptor (exige sesión)
src/main/resources/
├── templates/   fragments/{layout,componentes}, login, panel, ciudadanos/{lista,detalle,formulario},
│                reportes, error, error-api
├── static/      css/admin.css, js/validacion.js, js/estados.js
└── messages.properties
scripts/prueba-humo.sh
docs/            manual-usuario.md, pruebas.md, demo.md
```

### Componentes reutilizables (`templates/fragments`)

| Fragmento | Uso |
|---|---|
| `layout :: head('Título')`, `menu('activo')`, `encabezado('Título')` | Estructura común de todas las pantallas |
| `componentes :: alertas` | Mensajes `mensajeExito`, `mensajeInfo`, `mensajeError` |
| `componentes :: tabla(columnas, ~{::filas}, vacia, 'mensaje')` | Tabla con encabezados y filas propias |
| `componentes :: campo('propiedad', 'Etiqueta', 'tipo', reglas, soloLectura)` | Input con su mensaje de validación |
| `componentes :: selector('propiedad', 'Etiqueta', opciones)` | Lista desplegable con su mensaje de validación |
| `componentes :: consultaIdentificacion` | Consulta exacta por identificación (panel y listado) |
