# Decisiones de seguridad

- Autenticación stateless con JWT HMAC; `jti` persistido en PostgreSQL permite
  invalidar tokens en logout y entre reinicios.
- La version de seguridad del usuario forma parte del JWT y se compara con la
  base de datos en cada solicitud. Cambiar perfil o estado invalida todos sus
  tokens; un usuario inactivo tampoco puede autenticarse.
- `recordarContrasena` extiende únicamente la vida del token; nunca almacena ni
  prolonga la contraseña.
- `Usuario`, `Perfil`, `Permiso` y `DefensaPersonalSimulado` existentes se
  reutilizan. Flyway agrega revocación, versionado de seguridad, seis intentos
  de login y control de intentos de registro por IP.
- Defensa y correo se etiquetan **SIMULADOS**; Mailpit/SMTP permite probar el
  flujo sin convertirlo en una integracion oficial. Ningun adaptador expone
  una ruta publica.
- El administrador inicial se crea al iniciar solo si se proporcionan
  `ADMIN_INITIAL_IDENTIFICACION`, `ADMIN_INITIAL_EMAIL` y
  `ADMIN_INITIAL_PASSWORD`; la base solo recibe el hash BCrypt.
- El sexto fallo consecutivo bloquea el login por 15 minutos. Se permiten
  cinco intentos de registro por IP dentro de una ventana de 15 minutos.
- Los contadores y bloqueos se persisten en PostgreSQL y se actualizan con
  transacciones independientes, compartidos entre instancias y reinicios.
- Los cambios de perfil y estado se auditan con identificacion e IP; nunca se
  registran contrasenas, hashes ni JWT completos.
- CORS exige `CORS_ALLOWED_ORIGINS` explicito y rechaza `*`. Swagger y actuator
  solo se habilitan con el perfil `dev`; las cabeceras anti-sniff, frame deny y
  `Cache-Control: no-store` se aplican desde Spring Security.
- Las respuestas de error contienen exactamente `codigo`, `mensaje` y
  `detalles`.

Las cifras de la ejecucion y los hallazgos medidos se mantienen en
[`../pruebas/resultados-seguridad.md`](../pruebas/resultados-seguridad.md).
