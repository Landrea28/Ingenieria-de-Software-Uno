# Requisitos no funcionales de seguridad

- API stateless; JWT firmado con `JWT_SECRET` de al menos 32 bytes, expiración
  predeterminada de 15 minutos y de 8 horas cuando recordar está activo.
- Contraseñas con BCrypt. Las credenciales temporales tienen exactamente seis
  caracteres alfanuméricos, con mayúscula, minúscula y dígito.
- Cinco fallos consecutivos bloquean la identificación por 15 minutos; el
  estado persiste y se comparte entre instancias en PostgreSQL.
- Logout persiste el `jti` revocado hasta que expire el token.
- Respuestas 401 y 403 usan `codigo`, `mensaje` y `detalles`; login no revela si
  existe la identificación. Nunca se registra ni responde la contraseña.
- Solo administradores acceden a ciudadanía, reportes, usuarios y perfiles;
  personal autenticado consulta antecedentes.
- El validador de Defensa es **SIMULADO** y el correo se envía por SMTP
  configurable (Mailpit para pruebas).
- Los secretos se suministran por variables de entorno; no se versionan.
