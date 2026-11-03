CREATE TABLE auth_login_intento (
    identificacion VARCHAR(10) PRIMARY KEY,
    fallos INTEGER NOT NULL,
    ultimo_intento TIMESTAMP WITH TIME ZONE NOT NULL,
    bloqueado_hasta TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_auth_login_fallos CHECK (fallos BETWEEN 1 AND 5)
);

CREATE INDEX ix_auth_login_bloqueo ON auth_login_intento (bloqueado_hasta);
