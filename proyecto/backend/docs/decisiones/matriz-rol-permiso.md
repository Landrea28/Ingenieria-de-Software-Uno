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
| `/api/usuarios/**`, `/api/perfiles/**` | No | No | Sí |

Las reglas se aplican en Spring Security por URL y, en controladores existentes,
con `@PreAuthorize`. Los controladores de usuarios y perfiles aún no existen;
sus rutas ya quedan restringidas por URL.
