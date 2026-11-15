# Modelo conceptual — Sistema de Consulta Ciudadano

El modelo conceptual muestra las **entidades** del negocio y cómo se relacionan, sin detalles técnicos: no hay tipos de datos, claves foráneas ni tablas intermedias. Las relaciones muchos a muchos se dibujan directamente. El modelo entidad-relación (`modelo-er.md`) es su versión detallada, tal como existe en PostgreSQL.

## Diagrama

```mermaid
erDiagram
    CIUDADANO ||--o| ANTECEDENTE : "tiene"
    FUERZA_PUBLICA ||--o{ RANGO : "agrupa"
    FUERZA_PUBLICA |o--o{ USUARIO : "agrupa a"
    RANGO |o--o{ USUARIO : "clasifica a"
    FUERZA_PUBLICA ||--o{ PERSONAL_SIMULADO : "agrupa a"
    RANGO ||--o{ PERSONAL_SIMULADO : "clasifica a"
    USUARIO }o--o{ PERFIL : "tiene asignado"
    PERFIL }o--o{ PERMISO : "concede"
    OPCION_MENU }o--o{ PERMISO : "requiere"

    CIUDADANO {
        string identificacion
        string nombres
        string apellidos
        string datos_de_nacimiento
        string datos_de_expedicion
        string rh_y_grupo_sanguineo
        string estatura
    }
    ANTECEDENTE {
        string estado
        string fuente
        string fecha_actualizacion
    }
    FUERZA_PUBLICA {
        string codigo
        string nombre
    }
    RANGO {
        string nombre
    }
    USUARIO {
        string identificacion
        string nombres
        string apellidos
        string correo
        string id_fuerza
        string contrasena_con_hash
        string activo
    }
    PERSONAL_SIMULADO {
        string identificacion
        string nombres
        string apellidos
        string id_fuerza
        string activo
    }
    PERFIL {
        string codigo
        string nombre
    }
    PERMISO {
        string codigo
        string nombre
    }
    OPCION_MENU {
        string codigo
        string nombre
        string ruta
    }
```

`PERSONAL_SIMULADO` corresponde a la tabla `defensa_personal_simulado`: el listado **ficticio** que reemplaza la consulta al Departamento de Defensa.

## Cómo se lee

- **Ciudadano y antecedente.** Un ciudadano tiene como máximo un estado de antecedentes (REQUERIDO o NO_REQUERIDO). Si no tiene, significa «sin información».
- **Fuerza pública y rango.** Cada fuerza tiene varios rangos. Un usuario puede pertenecer a una fuerza y tener un rango, o no tener ninguno (el administrador inicial).
- **Usuario, perfil y permiso.** Un usuario puede tener varios perfiles, y cada perfil concede varios permisos. Las opciones del menú aparecen según los permisos que las habilitan.
- **Personal simulado.** Es el dato contra el que se valida el registro de nuevo personal. Cada integrante simulado pertenece a una fuerza y tiene un rango.

## Lo que este modelo deja fuera

Las tablas `token_revocado`, `auth_login_intento` y `auth_registro_intento` son de apoyo técnico a la seguridad (cierre de sesión, bloqueo de login y límite de registros). No representan conceptos del negocio, por eso solo aparecen en el modelo entidad-relación.
