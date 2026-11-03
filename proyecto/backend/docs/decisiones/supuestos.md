# Supuestos de seguridad

| Id | Supuesto | Por qué |
|---|---|---|
| S-01 | La verificación contra Defensa es **SIMULADA** con la tabla `DefensaPersonalSimulado`. | No hay acceso disponible a la fuente oficial y se requiere probar el flujo sin una integración externa. |
| S-02 | El correo usa SMTP de pruebas configurable, con Mailpit como opción local. | Permite validar entrega y contenido sin enviar credenciales a destinatarios reales durante desarrollo. |
| S-03 | La contraseña temporal generada tiene literalmente seis caracteres alfanuméricos, con al menos una mayúscula, minúscula y dígito. | Es una restricción explícita del contrato actual; no se cambia sin una decisión de producto. |
| S-06 | `recordarContrasena` extiende la expiración del token y nunca recuerda ni persiste la contraseña. | Evita almacenar credenciales reversibles en el cliente y limita la sesión mediante expiración. |
