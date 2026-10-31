# Sistema de Consulta Ciudadano

Monorepo del proyecto. Cada subproyecto tiene sus propias dependencias e instrucciones de ejecución.

```
proyecto/
├── backend/   # API REST (Java 21, Spring Boot, PostgreSQL)
├── web/       # aplicación web administrativa
└── mobile/    # aplicación móvil
```

## Orden de arranque

1. Base de datos y API: ver [backend/README.md](backend/README.md). La API queda en `http://localhost:8080` y su documentación en `http://localhost:8080/swagger-ui.html`.
2. Aplicaciones cliente: consumen la API REST del backend.

No subir contraseñas, tokens, archivos `.env` reales ni datos personales reales al repositorio.
