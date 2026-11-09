# Requisitos del backend

## Requerimientos funcionales

| Id | Requisito | Estado en el backend |
|---|---|---|
| RF01 | Solicitar registro del personal de fuerza pública | Endpoint y orquestación definidos; 503 sin la integración de seguridad |
| RF02 | Validar el registro contra Defensa y entregar credenciales | Esquema y dataset ficticio creados; validación pendiente de integración |
| RF03 | Iniciar sesión con opción «Recordar contraseña» | Contrato definido; 503 sin la integración de seguridad |
| RF04 | Consultar antecedentes por identificación | Implementado (`GET /api/antecedentes/{identificacion}`) |
| RF05 | Registrar, listar, buscar, consultar y actualizar ciudadanos | Implementado |
| RF06 | Gestionar usuarios, perfiles, opciones y permisos | Entidades y migraciones creadas; endpoints no incluidos |
| RF07 | Reporte PDF de ciudadanos filtrado por estado | Implementado (`GET /api/reportes/ciudadanos`) |

## Requerimientos no funcionales

| Id | Requisito | Meta medible |
|---|---|---|
| RNF-R1 | Rendimiento | Consultas por identificación y listados paginados responden en menos de 1 s en entorno local con los datos demo |
| RNF-R2 | Rendimiento | Generación del PDF en menos de 3 s con los datos demo |
| RNF-P1 | Portabilidad | Funciona en cualquier sistema con JDK 21 y PostgreSQL 15+; no depende del sistema operativo |
| RNF-P2 | Portabilidad | Configuración solo por variables de entorno (`.env.example`) |
| RNF-D1 | Disponibilidad | Arranque con un comando (`mvn spring-boot:run`) y base reconstruible desde cero con Flyway |
| RNF-D2 | Disponibilidad | `GET /api/health` indica que el servicio está activo |
| RNF-M1 | Mantenibilidad | Arquitectura por capas verificada automáticamente con ArchUnit |
