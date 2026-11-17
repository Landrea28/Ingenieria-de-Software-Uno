# Modelo entidad-relación — Sistema de Consulta Ciudadano

Base de datos: PostgreSQL. El esquema se crea con migraciones Flyway versionadas (V1 a V7) en `proyecto/backend/src/main/resources/db/migration/`. Este diagrama refleja el estado de esas migraciones.

Todos los datos de demostración son **ficticios** (perfil `demo`).

## Diagrama

```mermaid
erDiagram
    ciudadano ||--o| antecedente : "tiene estado"
    fuerza_publica ||--o{ rango : "agrupa"
    fuerza_publica |o--o{ usuario : "pertenece"
    rango |o--o{ usuario : "tiene"
    fuerza_publica ||--o{ defensa_personal_simulado : "pertenece"
    rango ||--o{ defensa_personal_simulado : "tiene"
    usuario ||--o{ usuario_perfil : "asigna"
    perfil ||--o{ usuario_perfil : "asignado a"
    perfil ||--o{ perfil_permiso : "concede"
    permiso ||--o{ perfil_permiso : "concedido a"
    opcion ||--o{ opcion_permiso : "requiere"
    permiso ||--o{ opcion_permiso : "habilita"

    ciudadano {
        BIGINT id PK
        VARCHAR(10) identificacion UK
        VARCHAR(100) nombres
        VARCHAR(100) apellidos
        DATE fecha_nacimiento
        VARCHAR(150) lugar_nacimiento
        DATE fecha_expedicion
        VARCHAR(150) lugar_expedicion
        VARCHAR(1) rh
        VARCHAR(2) grupo_sanguineo
        DECIMAL estatura
    }

    antecedente {
        BIGINT id PK
        BIGINT ciudadano_id FK
        VARCHAR(20) estado
        VARCHAR(50) fuente
        TIMESTAMPTZ fecha_actualizacion
    }

    fuerza_publica {
        VARCHAR(30) codigo PK
        VARCHAR(70) nombre
    }

    rango {
        BIGINT id PK
        VARCHAR(30) fuerza_codigo FK
        VARCHAR(80) nombre
    }

    usuario {
        BIGINT id PK
        VARCHAR(10) identificacion UK
        VARCHAR(100) nombres
        VARCHAR(100) apellidos
        VARCHAR(200) correo UK
        VARCHAR(30) fuerza_codigo FK
        BIGINT rango_id FK
        VARCHAR(40) id_fuerza
        VARCHAR(255) password_hash
        BOOLEAN activo
        TIMESTAMPTZ fecha_creacion
        BIGINT version_seguridad
    }

    perfil {
        BIGINT id PK
        VARCHAR(50) codigo UK
        VARCHAR(100) nombre
    }

    permiso {
        BIGINT id PK
        VARCHAR(60) codigo UK
        VARCHAR(100) nombre
    }

    opcion {
        BIGINT id PK
        VARCHAR(60) codigo UK
        VARCHAR(100) nombre
        VARCHAR(150) ruta
    }

    usuario_perfil {
        BIGINT usuario_id "PK, FK"
        BIGINT perfil_id "PK, FK"
    }

    perfil_permiso {
        BIGINT perfil_id "PK, FK"
        BIGINT permiso_id "PK, FK"
    }

    opcion_permiso {
        BIGINT opcion_id "PK, FK"
        BIGINT permiso_id "PK, FK"
    }

    defensa_personal_simulado {
        BIGINT id PK
        VARCHAR(10) identificacion UK
        VARCHAR(100) nombres
        VARCHAR(100) apellidos
        VARCHAR(30) fuerza_codigo FK
        BIGINT rango_id FK
        VARCHAR(40) id_fuerza
        BOOLEAN activo
    }

    token_revocado {
        VARCHAR(36) jti PK
        TIMESTAMPTZ expira_en
    }

    auth_login_intento {
        VARCHAR(10) identificacion PK
        INTEGER fallos
        TIMESTAMPTZ ultimo_intento
        TIMESTAMPTZ bloqueado_hasta
    }

    auth_registro_intento {
        VARCHAR(64) clave PK
        INTEGER intentos
        TIMESTAMPTZ ventana_inicia
    }
```

En el diagrama, `estatura` aparece como `DECIMAL`. En la base de datos es `NUMERIC(3,2)`, con valores entre 0.50 y 2.50 metros.

## Cómo leerlo

- **PK** es clave primaria, **FK** es clave foránea y **UK** es valor único.
- Un ciudadano tiene **como máximo un** antecedente (`antecedente.ciudadano_id` es único). Un ciudadano sin fila en `antecedente` significa «sin información» y nunca se interpreta como NO_REQUERIDO.
- La relación de `usuario` con `fuerza_publica` y con `rango` es **opcional** (el símbolo con círculo junto a `fuerza_publica` y `rango`): el administrador inicial no pertenece a ninguna fuerza. En `defensa_personal_simulado` sí es obligatoria.
- `usuario` se relaciona con `perfil` mediante `usuario_perfil`. `perfil` se relaciona con `permiso` mediante `perfil_permiso`. `opcion` (opción de menú) se relaciona con `permiso` mediante `opcion_permiso`. Son tres relaciones muchos a muchos.
- `defensa_personal_simulado` es el dataset **ficticio** que simula al Departamento de Defensa. El registro de personal se valida contra él.
- Las tablas `token_revocado`, `auth_login_intento` y `auth_registro_intento` son de apoyo a la seguridad (logout, bloqueo de login y límite de registros). No tienen clave foránea. `auth_login_intento.identificacion` coincide por valor con `usuario.identificacion`, pero no se declara como FK.

## Reglas principales del modelo

| Regla | Dónde se aplica |
|---|---|
| Identificación única de ciudadano | `ciudadano.identificacion` UNIQUE |
| Identificación y correo únicos de usuario | `usuario.identificacion` y `usuario.correo` UNIQUE |
| Un estado de antecedente por ciudadano | `antecedente.ciudadano_id` UNIQUE |
| Estados válidos | CHECK `estado IN ('REQUERIDO','NO_REQUERIDO')` |
| Rango sin repetirse dentro de una fuerza | UNIQUE `(fuerza_codigo, nombre)` en `rango` |
| Fecha de expedición posterior al nacimiento | CHECK en `ciudadano` |
