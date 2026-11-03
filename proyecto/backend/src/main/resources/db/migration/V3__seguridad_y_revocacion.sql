CREATE TABLE token_revocado (
    jti VARCHAR(36) PRIMARY KEY,
    expira_en TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_token_revocado_expira_en ON token_revocado (expira_en);

INSERT INTO perfil_permiso (perfil_id, permiso_id)
SELECT perfil.id, permiso.id
FROM perfil
CROSS JOIN permiso
WHERE perfil.codigo = 'ADMINISTRADOR'
ON CONFLICT DO NOTHING;

INSERT INTO perfil_permiso (perfil_id, permiso_id)
SELECT perfil.id, permiso.id
FROM perfil
JOIN permiso ON permiso.codigo = 'ANTECEDENTES_CONSULTAR'
WHERE perfil.codigo = 'PERSONAL_FUERZA_PUBLICA'
ON CONFLICT DO NOTHING;

INSERT INTO opcion_permiso (opcion_id, permiso_id)
SELECT opcion.id, permiso.id
FROM opcion
JOIN permiso ON (
    (opcion.codigo = 'CIUDADANOS' AND permiso.codigo IN
        ('CIUDADANOS_LEER', 'CIUDADANOS_CREAR', 'CIUDADANOS_ACTUALIZAR'))
    OR (opcion.codigo = 'ANTECEDENTES' AND permiso.codigo IN
        ('ANTECEDENTES_CONSULTAR', 'ANTECEDENTES_ACTUALIZAR'))
    OR (opcion.codigo = 'REPORTES' AND permiso.codigo = 'REPORTES_GENERAR')
    OR (opcion.codigo = 'SEGURIDAD' AND permiso.codigo = 'SEGURIDAD_ADMINISTRAR')
)
ON CONFLICT DO NOTHING;
