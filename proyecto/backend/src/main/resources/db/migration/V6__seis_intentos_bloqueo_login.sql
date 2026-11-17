ALTER TABLE auth_login_intento
    DROP CONSTRAINT ck_auth_login_fallos;

ALTER TABLE auth_login_intento
    ADD CONSTRAINT ck_auth_login_fallos CHECK (fallos BETWEEN 1 AND 6);
