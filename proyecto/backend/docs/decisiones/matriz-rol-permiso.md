# Matriz de rol y permiso

| Recurso | Público | PERSONAL_FUERZA_PUBLICA | ADMINISTRADOR |
|---|---:|---:|---:|
| `GET /api/health` | Sí | Sí | Sí |
| `POST /api/auth/login` | Sí | Sí | Sí |
| `POST /api/personal/registro` | Sí | Sí | Sí |
| `GET /api/catalogos/fuerzas-rangos` | Sí | Sí | Sí |
| `GET /api/antecedentes/{identificacion}` | No | Sí | Sí |
| `PUT /api/antecedentes/{identificacion}/estado` | No | No | Sí |
| `/api/ciudadanos/**` | No | No | Sí |
| `/api/reportes/**` | No | No | Sí |
| `/api/usuarios/**` | No | No | Sí |
| `/api/perfiles/**`, `/api/permisos/**` | No | No | Sí |

`POST /api/auth/logout` requiere token. Todas las rutas `/api/**` tienen regla
de acceso explícita; cualquier ruta API no declarada se deniega por defecto.
La consulta de antecedentes permite PERSONAL y ADMINISTRADOR solo en GET; los
cambios de estado siguen reservados a ADMINISTRADOR.
