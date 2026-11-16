# Decisiones y supuestos del backend

| Id | Decisión o supuesto | Justificación |
|---|---|---|
| S-01 | La identificación es `String` de 6 a 10 dígitos | Evita perder ceros iniciales |
| S-02 | La identificación no se modifica al actualizar y no hay borrado de ciudadanos | Es la clave de negocio |
| S-03 | El estado de antecedentes vive en una tabla propia, un registro por ciudadano | Permite cambiar la fuente sin tocar `ciudadano` |
| S-04 | Sin registro de antecedente se responde 404 `SIN_INFORMACION`, nunca `NO_REQUERIDO` | La ausencia de dato no equivale a «no requerido» |
| S-05 | Todos los antecedentes y datos de Defensa son simulados (fuente `SIMULADA`) | No hay acceso a fuentes oficiales |
| S-06 | `PUT /api/antecedentes/{id}/estado` existe solo para demostraciones | Debe protegerse antes de cualquier despliegue |
| S-07 | Tomcat embebido en Spring Boot (JAR ejecutable) | Cumple el requisito de servidor de aplicaciones |
| S-08 | Esquema gestionado con Flyway; Hibernate solo valida | Base reconstruible desde cero; cambios de esquema con migración nueva |
| S-09 | El perfil `demo` carga datos ficticios y solo debe usarse sobre una base limpia | Evita mezclar datos de prueba con datos que se deban conservar |
| S-10 | El servidor escucha en `127.0.0.1` por defecto | Limita la exposición de desarrollo; la seguridad de rutas JWT está habilitada, pero las integraciones externas siguen siendo simuladas |
| S-11 | Si no hay ciudadanos para el filtro solicitado, el reporte responde HTTP 404 con código `SIN_RESULTADOS` en lugar de generar un PDF vacío | Distingue una consulta válida sin datos de un reporte generado y permite que la web muestre un aviso informativo claro al administrador |
| S-12 | Los perfiles son fijos; el administrador puede consultar perfiles y administrar los permisos asociados, pero no crear ni eliminar perfiles | Mantiene estable el catálogo de roles que utiliza la autorización y evita que la administración de seguridad introduzca roles sin reglas de acceso definidas |
| S-13 | El correo usa SMTP configurable; Mailpit es el servidor local de pruebas | Permite verificar la entrega de credenciales sin enviarlas a destinatarios reales durante el desarrollo |
| S-14 | La contraseña temporal tiene exactamente seis caracteres e incluye mayúscula, minúscula y número | Corresponde a la política implementada y a las pruebas del generador; cualquier cambio requiere una decisión de producto |
| S-15 | `recordarContrasena` extiende la vigencia del JWT, no guarda ni persiste la contraseña | Mantiene la sesión limitada por expiración sin conservar credenciales reversibles |
