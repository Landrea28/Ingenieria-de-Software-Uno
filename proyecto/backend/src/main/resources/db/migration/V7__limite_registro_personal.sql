CREATE TABLE auth_registro_intento (
    clave VARCHAR(64) PRIMARY KEY,
    intentos INTEGER NOT NULL,
    ventana_inicia TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_auth_registro_intentos CHECK (intentos BETWEEN 1 AND 6)
);
