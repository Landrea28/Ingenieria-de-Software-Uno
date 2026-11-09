# Requisitos no funcionales de seguridad

- API stateless; JWT firmado con `JWT_SECRET` de al menos 32 bytes, expiración
  predeterminada de 15 minutos y de 8 horas cuando recordar está activo.
- Contraseñas con BCrypt. Las credenciales temporales tienen exactamente seis
  caracteres alfanuméricos, con mayúscula, minúscula y dígito.
- Seis fallos consecutivos bloquean la identificación por 15 minutos; el
  estado persiste y se comparte entre instancias en PostgreSQL.
- El registro de personal permite cinco intentos por IP cada 15 minutos; los
  intentos excedidos producen un error controlado.
- Logout persiste el `jti` revocado hasta que expire el token.
- Cambiar perfil o estado incrementa la versión de seguridad y rechaza tokens
  anteriores; usuarios inactivos no autentican.
- Respuestas 401 y 403 usan `codigo`, `mensaje` y `detalles`; login no revela si
  existe la identificación. Nunca se registra ni responde la contraseña.
- Solo administradores acceden a ciudadanía, reportes, usuarios y perfiles;
  personal autenticado consulta antecedentes.
- El validador de Defensa y el servicio de correo estan etiquetados
  **SIMULADOS**; Mailpit/SMTP configurable es solo un medio de prueba.
- CORS acepta solo los origenes explicitos de `CORS_ALLOWED_ORIGINS`, nunca
  `*`. Swagger y actuator no se exponen fuera del perfil `dev`.
- Se envian `X-Content-Type-Options`, `X-Frame-Options` y `Cache-Control:
  no-store` en los endpoints protegidos y de autenticacion.
- Los secretos se suministran por variables de entorno; no se versionan.
