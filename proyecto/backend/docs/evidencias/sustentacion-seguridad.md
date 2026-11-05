# Sustentación de seguridad — Día 12

## Flujo y decisiones

El cliente envía `identificacion`, `contrasena` y `recordarContrasena` a
`POST /api/auth/login`. El servicio compara la contraseña con BCrypt y devuelve
un JWT firmado con TTL corto o largo. `FiltroJwt` valida firma, expiración,
revocación, estado y versión del usuario. Spring Security aplica la regla de
la ruta: PERSONAL consulta antecedentes; solo ADMINISTRADOR gestiona ciudadanos,
reportes, usuarios y permisos. Logout revoca el `jti`; un cambio de perfil o
estado incrementa la versión y rechaza los JWT anteriores.

BCrypt se usa en vez de SHA-256 porque es una función de derivación de
contraseñas con salt y coste configurable; SHA-256 es demasiado rápido para
proteger hashes de contraseñas frente a intentos fuera de línea. La clave
temporal se construye con `SecureRandom`: se elige al menos una mayúscula,
minúscula y dígito, se completan seis caracteres alfanuméricos y se mezclan.
La base solo recibe el hash. «Recordar» cambia la expiración del token y nunca
guarda la contraseña.

La consulta de Defensa y el correo están **SIMULADOS**. La base ficticia prueba
aceptado/rechazado/no disponible; GreenMail prueba el envío sin un servicio
de correo real. JUnit, MockMvc, H2 y Spring Security Test validan errores,
autorización, bloqueo, JWT, hash, salida PDF, reglas de administración y el
contenido de logs. La colección Postman cubre las comprobaciones negativas.

## Diez preguntas y respuestas breves

1. **¿Qué autentica al usuario?** — La contraseña validada contra su hash BCrypt.
2. **¿Qué hace el filtro JWT?** — Verifica firma, vencimiento, revocación, estado y versión.
3. **¿Por qué BCrypt y no SHA-256?** — BCrypt es lento y con salt; SHA-256 es demasiado rápido para contraseñas.
4. **¿Cómo se genera la temporal?** — Seis caracteres aleatorios; al menos una mayúscula, una minúscula y un número.
5. **¿Qué significa recordar?** — Aumenta el TTL del JWT, no persiste credenciales.
6. **¿Cómo se revoca una sesión?** — Se registra el `jti` en logout; perfil/estado cambia la versión de seguridad.
7. **¿Qué queda público?** — Salud, login, registro personal y catálogo de fuerzas/rangos.
8. **¿Qué parte es simulada?** — La validación de Defensa y el transporte de correo de prueba.
9. **¿Cómo se probó?** — `mvn test`, H2, MockMvc, GreenMail, matriz de roles y verificaciones de hash/logs.
10. **¿Qué falta para producción?** — Revisar secretos, configuración, dependencias y retención de logs; ZAP es un baseline opcional.
