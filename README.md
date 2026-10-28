# Sistema de Consulta Ciudadano

Sistema para la **Registraduría** que permite al personal de la fuerza pública consultar, leyendo el código de barras de la cédula, si un ciudadano está **Requerido** o **No requerido**. También permite a un administrador gestionar ciudadanos y generar reportes en PDF.

> Proyecto académico de **Ingeniería de Software I** — Fundación Universitaria Konrad Lorenz.

---

## Objetivo

Ofrecer una consulta rápida y confiable del estado de un ciudadano en campo, con un módulo administrativo centralizado para mantener la información actualizada y generar reportes.

## Actores

| Actor | Descripción |
|---|---|
| Personal de la fuerza pública | Se registra, se autentica y consulta ciudadanos escaneando la cédula desde la app móvil. |
| Administrador del sistema | Gestiona ciudadanos, usuarios y perfiles, y solicita reportes PDF desde la app web. |
| Departamento de Defensa | Valida al personal de la fuerza pública. **Integración simulada** (ver [Servicios externos](#servicios-externos-simulados)). |

## Arquitectura

Monorepo con un **backend único (API REST)** consumido por dos clientes:

```
 App web (admin) ──┐
                   ├──► API REST (Spring Boot) ──► ORM (Hibernate/JPA) ──► PostgreSQL
 App móvil ────────┘
```

- La **lógica de negocio y las reglas de acceso viven en el servidor**, no en los clientes.
- El backend es el único que accede a la base de datos, y lo hace mediante ORM.

## Funcionalidades por entrega

| Entrega | Funcionalidades |
|---|---|
| **MVP 1** | Registro y autenticación del personal · Registro de ciudadanos · Consulta de antecedentes por código de barras |
| **MVP 2** | Administración y control de usuarios y perfiles |
| **MVP 3** | Reporte PDF filtrado por estado (Requerido / No requerido) |

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java + Spring Boot (Spring MVC) + API REST |
| Web | Spring MVC + Thymeleaf (vistas del lado servidor), consume la API REST |
| Móvil | React Native + Expo (Android como plataforma de prueba) |
| Base de datos | PostgreSQL |
| ORM | Hibernate (Spring Data JPA) |
| Servidor de aplicaciones | Tomcat (embebido en Spring Boot) |
| Seguridad | Spring Security · contraseñas cifradas con bcrypt |

## Estructura del repositorio

```
sistema-consulta-ciudadano/
├── backend/    # API REST con Spring Boot
├── web/        # App web administrativa (Spring MVC + Thymeleaf)
├── mobile/     # App móvil (React Native + Expo)
├── database/   # schema.sql, seed.sql y modelo de datos
├── docs/       # Arquitectura, API, decisiones, actas, presentación y evidencias
├── .gitignore
└── README.md
```

Cada subproyecto tiene sus propios archivos de dependencias y sus propios comandos de ejecución. Las instrucciones detalladas están en:

- [backend/README.md](backend/README.md)
- [web/README.md](web/README.md)
- [mobile/README.md](mobile/README.md)

## Requisitos previos

| Herramienta | Versión | Usada en |
|---|---|---|
| Git | — | Todo el repo |
| Java (JDK) | <<COMPLETAR>> | backend, web |
| Maven / Gradle | <<COMPLETAR: cuál y versión>> | backend, web |
| PostgreSQL | <<COMPLETAR>> | database |
| Node.js + npm | <<COMPLETAR>> | mobile |
| Expo (Expo Go en Android) | <<COMPLETAR>> | mobile |

## Instalación y orden de arranque

```bash
git clone https://github.com/Landrea28/Ingenieria-de-Software-Uno.git
cd Ingenieria-de-Software-Uno
```

Los componentes se levantan **en este orden**, porque cada uno depende del anterior.

### 1. Base de datos

```bash
# Crear la base de datos
<<COMPLETAR: p. ej. createdb nombre_bd>>

# Crear las tablas
<<COMPLETAR: p. ej. psql -d nombre_bd -f database/schema.sql>>

# Cargar datos de prueba
<<COMPLETAR: p. ej. psql -d nombre_bd -f database/seed.sql>>
```

### 2. Backend (API REST)

```bash
cd backend
<<COMPLETAR: comando de ejecución>>
```

La API queda disponible en: <<COMPLETAR: URL y puerto>>

### 3. Clientes (con el backend ya en ejecución)

**Web:**

```bash
cd web
<<COMPLETAR: comando de ejecución>>
```

**Móvil:**

```bash
cd mobile
<<COMPLETAR: instalar dependencias>>
<<COMPLETAR: iniciar Expo>>
```

## Variables de entorno y secretos

- Cada subproyecto que lo necesite tiene un **`.env.example`** con los nombres de las variables y valores de ejemplo, **sin datos reales**.
- Para ejecutar en local, copia `.env.example` a **`.env`** y completa tus valores.
- **No subas al repositorio** archivos `.env`, credenciales, tokens, contraseñas ni datos personales reales. Los datos de prueba de `seed.sql` deben ser ficticios.

## Servicios externos (simulados)

Los siguientes servicios **no son integraciones reales**: están **simulados** para el alcance académico del proyecto.

- **Departamento de Defensa:** validación del personal de la fuerza pública.
- **Correo electrónico:** envío de notificaciones.

Su diseño y la justificación de la simulación están documentados en [docs/decisiones](docs/decisiones).

## Estrategia de ramas (Git)

| Rama | Propósito |
|---|---|
| `main` | Versión estable. Solo recibe cambios desde `develop` al cerrar una entrega. |
| `develop` | Rama de integración del trabajo del equipo. |
| `feature/<id>-<descripcion>` | Una funcionalidad concreta. Se crea desde `develop`. |

Ejemplos de ramas `feature`: `feature/HU04-registro-ciudadano`, `feature/web-ciudadanos`, `feature/HU07-escaneo`, `feature/RF07-reporte-pdf`.

**Flujo:** cada `feature/...` se integra a `develop` mediante **Pull Request**, que debe ser **revisado y aprobado por al menos un compañero** antes de hacer merge.

```bash
git checkout develop
git pull
git checkout -b feature/HU04-registro-ciudadano
# ... cambios y commits ...
git push -u origin feature/HU04-registro-ciudadano
# Abrir Pull Request hacia develop
```

## Equipo

| Integrante | Módulo |
|---|---|
| Sebastián | Backend / API REST |
| Lina | Aplicación web |
| Mariana | Aplicación móvil |
| Julián | Seguridad |
| David | Datos / ORM / Reporte PDF |

## Estado del proyecto

🚧 **En desarrollo.**

| Versión | Entrega |
|---|---|
| v0.1 | MVP 1 |
| v0.2 | MVP 2 |
| v1.0 | MVP 3 |

Versión actual: <<COMPLETAR>>
