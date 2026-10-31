# Especificación de clases

## Dominio (`*.entity`)

| Clase | Descripción |
|---|---|
| `Ciudadano` | Persona registrada: identificación única, nombres, apellidos, fechas y lugares de nacimiento y expedición, RH, grupo sanguíneo y estatura |
| `Antecedente` | Estado `REQUERIDO` / `NO_REQUERIDO` de un ciudadano (uno por ciudadano), con fuente y fecha de actualización |
| `FuerzaPublica`, `Rango` | Catálogo de fuerzas y sus rangos |
| `DefensaPersonalSimulado` | Dataset ficticio del Departamento de Defensa |
| `Usuario`, `Perfil`, `Permiso`, `Opcion` | Modelo de seguridad: usuario con perfiles, perfiles con permisos y opciones de menú con permisos |

## Servicios (`*.service`)

| Clase | Responsabilidad |
|---|---|
| `CiudadanoService` | Crear, buscar, listar y actualizar; valida fechas y unicidad de la identificación |
| `AntecedenteService` | Consultar el estado (404 `SIN_INFORMACION` si no existe) y establecer el estado de demostración |
| `CatalogoService` | Consultar fuerzas y rangos y validar que coincidan |
| `ReporteService` | Construir el PDF de ciudadanos por estado |
| `PersonalService` | Orquestar el registro de personal con las integraciones |
| `RegistroPersonalIntegracion`, `AutenticacionIntegracion` | Contratos reemplazables de validación, credenciales, correo y autenticación; hoy responden 503 |

## Transversales

| Clase | Responsabilidad |
|---|---|
| `GlobalExceptionHandler`, `ErrorResponse`, `CodigoError`, `NegocioException` | Formato uniforme de errores y códigos HTTP |
| `CorsConfig` | CORS de `/api/**` según `CORS_ALLOWED_ORIGINS` |
| `HealthController` | Estado del servicio |
