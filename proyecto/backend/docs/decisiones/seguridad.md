# Decisiones de seguridad

- Autenticación stateless con JWT HMAC; `jti` persistido en PostgreSQL permite
  invalidar tokens en logout y entre reinicios.
- `recordarContrasena` extiende únicamente la vida del token; nunca almacena ni
  prolonga la contraseña.
- `Usuario`, `Perfil`, `Permiso` y `DefensaPersonalSimulado` existentes se
  reutilizan. Flyway V3 agrega revocación y asociaciones perfil-permiso.
- Defensa es **SIMULADO**; el adaptador SMTP usa Mailpit en desarrollo. Ningún
  adaptador expone una ruta nueva.
- El administrador inicial se crea al iniciar solo si se proporcionan
  `ADMIN_INITIAL_IDENTIFICACION`, `ADMIN_INITIAL_EMAIL` y
  `ADMIN_INITIAL_PASSWORD`; la base solo recibe el hash BCrypt.
- Los contadores y bloqueos de login se persisten en PostgreSQL y se actualizan
  con transacciones independientes, compartidos entre instancias y reinicios.
- Las respuestas de error contienen exactamente `codigo`, `mensaje` y
  `detalles`.

## Resultados de pruebas

Verificado con Java 21 y Maven 3.9.9:

- `mvn test`: 25 pruebas, 0 fallos y 0 errores (antes de agregar las pruebas
  adicionales de respuestas de Defensa y entradas maliciosas, que se ejecutan
  en la verificación final).
- `mvn package`: compilación, suite de pruebas y empaquetado completados.
- Cobertura de seguridad: 1.000 contraseñas generadas; login válido/inválido,
  bloqueo, expiración y revocación JWT, autorización por rol, registro SIMULADO,
  envío SMTP local GreenMail, hash BCrypt y ausencia de la contraseña en
  respuestas y logs.
