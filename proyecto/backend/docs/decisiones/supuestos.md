# Supuestos de seguridad

| Id | Supuesto | Por qué |
|---|---|---|
| S-01 | La verificación contra Defensa es **SIMULADA** con la tabla `DefensaPersonalSimulado`. | No hay acceso disponible a la fuente oficial y se requiere probar el flujo sin una integración externa. |
| S-02 | El correo usa SMTP de pruebas configurable, con Mailpit como opción local. | Permite validar entrega y contenido sin enviar credenciales a destinatarios reales durante desarrollo. |
| S-03 | La contraseña temporal generada tiene literalmente seis caracteres alfanuméricos, con al menos una mayúscula, minúscula y dígito. | Es una restricción explícita del contrato actual; no se cambia sin una decisión de producto. |
| S-06 | `recordarContrasena` extiende la expiración del token y nunca recuerda ni persiste la contraseña. | Evita almacenar credenciales reversibles en el cliente y limita la sesión mediante expiración. |
| S-07 | Los usuarios no se eliminan; se marcan `INACTIVO`. | Conserva trazabilidad de auditoría y evita romper referencias históricas. |
| S-08 | Siempre debe existir al menos un `ADMINISTRADOR` activo. | Evita perder la capacidad operativa para administrar accesos y permisos. |
| S-09 | La auditoría registra identificación e IP, pero excluye contraseñas, hashes y tokens completos. | Permite investigar accesos sin convertir los logs en un repositorio de credenciales. |
| S-10 | CORS se configura mediante `CORS_ALLOWED_ORIGINS` con orígenes explícitos y sin comodín. | Permite despliegues distintos sin ampliar el acceso de navegador a cualquier origen. |
