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
| S-10 | El servidor escucha en `127.0.0.1` por defecto | La API no tiene autorización todavía |
