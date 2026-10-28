# Sistema de Consulta Ciudadano — Aplicación Web y Base de Datos

Sistema que permite al personal de la fuerza pública consultar si un ciudadano está **Requerido** o **No requerido** leyendo el código de barras de la cédula, y que permite a un administrador gestionar la información de los ciudadanos y generar reportes en PDF.

> Proyecto académico de **Ingeniería de Software I** — Fundación Universitaria Konrad Lorenz.

---

## Objetivo

Ofrecer una herramienta confiable y rápida para verificar en campo el estado de requerimiento de un ciudadano, junto con un módulo administrativo web para mantener actualizada la información y generar reportes.

## Actores

| Actor | Descripción |
|---|---|
| Personal de la fuerza pública | Consulta en campo el estado del ciudadano escaneando la cédula (app móvil). |
| Administrador del sistema | Registra, consulta y actualiza ciudadanos, y solicita reportes en PDF (app web). |
| Departamento de Defensa | Valida la identidad y habilitación del personal de la fuerza pública. |

## Alcance de este repositorio

El sistema está dividido en **dos repositorios**:

| Repositorio | Contenido |
|---|---|
| **Este repositorio** | Aplicación **web** de administración + **base de datos**. |
| Repositorio móvil | Aplicación **móvil** de consulta en campo + su base de datos: <<enlace al repo móvil>> |

Este README documenta únicamente la parte web y la base de datos.

## Funcionalidades (web)

- **Registro de ciudadanos:** alta de nuevos ciudadanos con su estado (Requerido / No requerido).
- **Consulta de ciudadanos:** listado y búsqueda de ciudadanos registrados.
- **Actualización de ciudadanos:** edición de los datos y del estado de un ciudadano.
- **Solicitud de reportes PDF:** generación de reportes filtrados por estado (Requerido / No requerido).

## Tecnologías

| Componente | Tecnología |
|---|---|
| Frontend web | <<COMPLETAR>> |
| Backend web | <<COMPLETAR>> |
| Base de datos | <<COMPLETAR>> |
| ORM / acceso a datos | <<COMPLETAR>> |
| Generación de PDF | <<COMPLETAR>> |
| Control de versiones | Git + GitHub |

## Estructura del repositorio

```
Ingenieria-de-Software-Uno/
├── web/        # Código fuente de la aplicación web de administración
├── database/   # Modelo de datos, schema.sql (creación) y seed.sql (datos de prueba)
├── docs/       # Documentación del proyecto (requisitos, diagramas, etc.)
└── README.md   # Este archivo
```

## Requisitos previos

- Git
- <<COMPLETAR: entorno de ejecución de la web y versión (p. ej. runtime, gestor de paquetes)>>
- <<COMPLETAR: motor de base de datos y versión>>

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/Landrea28/Ingenieria-de-Software-Uno.git
cd Ingenieria-de-Software-Uno
```

### 2. Crear la base de datos

```bash
# Crear la base de datos
<<COMPLETAR: comando para crear la base de datos>>

# Crear las tablas
<<COMPLETAR: comando para ejecutar database/schema.sql>>

# Cargar datos de prueba
<<COMPLETAR: comando para ejecutar database/seed.sql>>
```

### 3. Configurar la aplicación web

```bash
cd web
<<COMPLETAR: comando para instalar dependencias>>
```

Configurar la conexión a la base de datos: <<COMPLETAR: archivo de configuración / variables de entorno>>

### 4. Ejecutar la aplicación web

```bash
<<COMPLETAR: comando para ejecutar la aplicación>>
```

La aplicación quedará disponible en: <<COMPLETAR: URL local, p. ej. http://localhost:PUERTO>>

## Estrategia de ramas (Git)

| Rama | Propósito |
|---|---|
| `main` | Versión estable. Solo recibe cambios probados desde `develop`. |
| `develop` | Rama de integración de las funcionalidades en desarrollo. |
| `feature/<nombre>` | Desarrollo de una funcionalidad concreta. Se crea desde `develop` y se integra de vuelta mediante Pull Request. Ejemplo: `feature/web-ciudadanos`. |

Flujo básico:

```bash
git checkout develop
git checkout -b feature/web-ciudadanos
# ... cambios y commits ...
git push -u origin feature/web-ciudadanos
# Abrir Pull Request hacia develop
```

## Equipo

| Integrante | Módulo / Responsabilidad |
|---|---|
| <<COMPLETAR>> | <<COMPLETAR>> |
| <<COMPLETAR>> | <<COMPLETAR>> |
| <<COMPLETAR>> | <<COMPLETAR>> |
| <<COMPLETAR>> | <<COMPLETAR>> |

## Estado del proyecto

🚧 **En desarrollo.**
