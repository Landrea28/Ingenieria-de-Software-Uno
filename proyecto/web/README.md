# Aplicación web administrativa

Aplicación del administrador del Sistema de Consulta Ciudadano. Usa Spring MVC con vistas Thymeleaf del lado del servidor y consume la API REST de `../backend`. **No accede directamente a la base de datos.**

> Estado actual: **login, listado y registro conectados a la API real**. La edición (PUT) y los reportes todavía son simulados.

## Requisitos

- JDK 21 o superior (`java -version`)
- No hace falta instalar Maven: se usa el wrapper incluido (`mvnw`)

## Configuración

| Variable                 | Valor por defecto       | Descripción                                  |
|--------------------------|-------------------------|----------------------------------------------|
| `API_BASE_URL`           | `http://localhost:8080` | URL base de la API REST del backend          |
| `SERVER_PORT`            | `8081`                  | Puerto de la web (el backend usa el 8080)    |
| `SPRING_PROFILES_ACTIVE` | `api`                   | `api` = backend real; `mock` = datos simulados sin backend |

`.env.example` muestra los valores. Para cambiarlos, exporta las variables antes de ejecutar:

```bash
export API_BASE_URL=http://localhost:8080
```

No subas archivos `.env` con valores reales; ya están en `.gitignore`.

## Ejecutar

```bash
cd proyecto/web
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

1. Levantar primero el backend (ver [`../backend/README.md`](../backend/README.md)). Con el perfil `demo` del backend se cargan 19 ciudadanos ficticios.
2. Abrir <http://localhost:8081/login> e ingresar con un usuario **ADMINISTRADOR** del backend (por ejemplo el admin inicial configurado con `ADMIN_INITIAL_*`).

Sin backend, para una demo: `SPRING_PROFILES_ACTIVE=mock ./mvnw spring-boot:run`. Con ese perfil el login acepta cualquier credencial y todas las pantallas muestran **DATOS SIMULADOS — PRUEBA**.

> El backend usa Lombok y necesita un **JDK 21** para compilar; con JDK más nuevos falla (`EndPosTable`). La web compila con JDK 21 o superior.

## Conexión con la API

| Pantalla | Endpoint |
|---|---|
| Login / Cerrar sesión | `POST /api/auth/login`, `POST /api/auth/logout` |
| Listado y búsqueda (paginado de 20) | `GET /api/ciudadanos?busqueda=&pagina=&tamano=` |
| Registrar | `POST /api/ciudadanos` |
| Editar (carga de datos) | `GET /api/ciudadanos/{identificacion}` |

- **Sesión:** el token JWT se guarda en la sesión del servidor web (`SesionAdmin`); el navegador solo recibe la cookie `JSESSIONID` (`HttpOnly`, `SameSite=Lax`, sin `;jsessionid` en las URL).
- **Solo administradores:** la respuesta del login no incluye el rol, así que tras el login se hace una consulta que exige `ADMINISTRADOR`; si la API responde 403 se cierra la sesión y se muestra "Su usuario no tiene permisos de administrador".
- **401 / 403 en cualquier pantalla** (token vencido, revocado o sin rol): se cierra la sesión y se redirige a `/login` con un mensaje (`ManejoErroresApi`).
- **Errores:** `TraductorErroresApi` convierte cada respuesta en una excepción: 400 → validación (los `detalles` "campo: mensaje" se muestran en su campo), 404 → no encontrado, 409 → duplicado, 5xx / red / tiempo agotado (3 s conexión, 10 s lectura) → "No fue posible conectar con el servidor".

### Estados de la interfaz

| Estado | Cómo se ve |
|---|---|
| Carga | Barra de progreso arriba y contenido atenuado al enviar formularios o paginar (`js/estados.js`) |
| Vacío | "Aún no hay ciudadanos registrados" o "No hay resultados para «…»", con botón para registrar |
| Éxito | Mensaje verde y la fila nueva resaltada (el listado se filtra por la identificación registrada) |
| Error | Mensaje rojo con **Reintentar**; en el registro se conservan los datos escritos |

## Pruebas

```bash
./mvnw test
```

| Prueba | Qué cubre |
|---|---|
| `ClientesRestTest` | Clientes REST contra un servidor HTTP simulado: URL, token, JSON del contrato y traducción de 400/401/403/404/409/5xx/red |
| `AccesoYEstadosTest` | Redirección al login, solo administradores, 401/403 en pantallas, estados vacío y error |
| `RegistroCiudadanoTest` | Validaciones del formulario y registro con confirmación (mock) |
| `VistasTest` | Cada pantalla responde y renderiza su vista |

## Estructura

```
src/main/java/co/edu/konrad/consultaciudadano/web/
├── config/      ApiProperties, ApiRestConfig (RestClient), TraductorErroresApi, WebConfig
├── controller/  Login, Panel, Ciudadano, Reporte + ModeloGlobal y ManejoErroresApi (401/403/red)
├── dto/         CiudadanoDto, PaginaCiudadanos, LoginResponse, ErrorApi (esquemas del contrato OpenAPI)
├── form/        CiudadanoForm y ReglasCiudadano
├── service/     CiudadanoApiClient y AuthApiClient (interfaces) con implementación Rest (perfil "api")
│                y Mock (perfil "mock"), AccesoAdminService y excepciones Api*
└── sesion/      SesionAdmin (token en la sesión) y SesionInterceptor (exige sesión)
src/main/resources/
├── templates/
│   ├── fragments/layout.html       estructura: head, barra de navegación, encabezado
│   ├── fragments/componentes.html  componentes: alertas, tabla, campo, selector
│   └── login, panel, ciudadanos/{lista,formulario}, reportes
├── messages.properties             textos de error de conversión
└── static/css/  admin.css
```

### Componentes reutilizables

| Fragmento | Uso |
|---|---|
| `layout :: menu('activo')` | Barra de navegación lateral; resalta la opción actual |
| `componentes :: alertas` | Muestra `mensajeExito`, `mensajeInfo` y `mensajeError` (modelo o flash) |
| `componentes :: tabla(columnas, ~{::filas}, vacia, 'mensaje')` | Tabla con encabezados, filas propias y mensaje cuando no hay datos |
| `componentes :: campo('propiedad', 'Etiqueta', 'tipo', soloLectura)` | Input con su mensaje de validación (dentro de un `th:object`) |
| `componentes :: selector('propiedad', 'Etiqueta', opciones)` | Select con su mensaje de validación |

## Registro y validaciones

- **Registrar** envía `POST /api/ciudadanos` (perfil `api`) y vuelve al listado con un mensaje de confirmación y la fila nueva resaltada. Con el perfil `mock` se guarda solo en memoria y se pierde al reiniciar. Una identificación repetida (409) muestra "Ya existe un ciudadano con esta identificación."
- **Guardar cambios** (edición) sigue siendo simulado.
- Las reglas viven en `form/ReglasCiudadano.java` y se aplican en dos capas:
  1. **Navegador** (`static/js/validacion.js`): atributos HTML5 (`required`, `pattern`, `min`, `max`, `step`, `maxlength`) con mensajes en español junto a cada campo. Valida al salir del campo y al enviar.
  2. **Servidor web** (`CiudadanoForm` con Bean Validation): mismas reglas, por si el navegador no ejecuta JavaScript.

  El backend vuelve a validar todo; la validación de la web solo mejora la experiencia.

| Campo | Regla |
|---|---|
| identificacion | obligatorio, 6 a 10 dígitos |
| nombres, apellidos | obligatorio, máx. 100, solo letras y espacios |
| fechaNacimiento | obligatorio, no futura |
| fechaExpedicion | obligatorio, no futura, posterior a la fecha de nacimiento |
| lugarNacimiento, lugarExpedicion | obligatorio, máx. 150 |
| rh | `+` o `-` (lista) |
| grupoSanguineo | A, B, AB u O (lista) |
| estatura | 0.50 a 2.50 m, máx. 2 decimales |

## Pantallas

| Ruta                                | Pantalla                             |
|-------------------------------------|--------------------------------------|
| `/login`                            | Ingreso del administrador            |
| `/`                                 | Panel principal                      |
| `/ciudadanos?busqueda=`             | Listado y búsqueda                   |
| `/ciudadanos/nuevo`                 | Registrar ciudadano                  |
| `/ciudadanos/{identificacion}/editar` | Editar ciudadano                   |
| `/reportes`                         | Solicitar reporte (Requerido / No requerido) |

## Datos simulados

`CiudadanoApiClientMock` tiene 3 ciudadanos ficticios (`1000000001` a `1000000003`, nombres "Prueba …"). No coinciden con el seed del backend (`9000000001…`). Mientras el perfil `mock` esté activo, todas las pantallas muestran el aviso **DATOS SIMULADOS — PRUEBA**.
