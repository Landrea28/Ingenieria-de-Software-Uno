# Aplicación web administrativa

Aplicación del administrador del Sistema de Consulta Ciudadano. Usa Spring MVC con vistas Thymeleaf del lado del servidor y consume la API REST de `../backend`. **No accede directamente a la base de datos.**

> Estado actual (día 1): esqueleto con **datos simulados (mock)**. Login, guardado y reportes son placeholders.

## Requisitos

- JDK 21 o superior (`java -version`)
- No hace falta instalar Maven: se usa el wrapper incluido (`mvnw`)

## Configuración

| Variable                 | Valor por defecto       | Descripción                                  |
|--------------------------|-------------------------|----------------------------------------------|
| `API_BASE_URL`           | `http://localhost:8080` | URL base de la API REST del backend          |
| `SERVER_PORT`            | `8081`                  | Puerto de la web (el backend usa el 8080)    |
| `SPRING_PROFILES_ACTIVE` | `mock`                  | `mock` = datos simulados de prueba           |

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

Abrir <http://localhost:8081/login>. Por ahora "Ingresar" entra al panel sin validar credenciales.

## Pruebas

```bash
./mvnw test
```

`VistasTest` (MockMvc) verifica que cada pantalla responda y renderice su vista con los datos mock.

## Estructura

```
src/main/java/co/edu/konrad/consultaciudadano/web/
├── config/      ApiProperties (api.base-url ← API_BASE_URL)
├── controller/  Login, Panel, Ciudadano, Reporte + ModeloGlobal (aviso "datos simulados")
├── dto/         CiudadanoDto (mismos campos que CiudadanoResponse del backend)
├── form/        CiudadanoForm (objeto que respalda el formulario de registro/edición)
└── service/     CiudadanoApiClient (interfaz) + CiudadanoApiClientMock (perfil "mock")
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

"Guardar" en el formulario es **simulado**: no envía nada a la API y vuelve al listado con un aviso "SIMULACIÓN".

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
