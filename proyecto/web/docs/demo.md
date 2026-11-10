# Demo e integración — Web administrativa

## Antes de la sustentación (el día anterior)

1. Backend en ejecución con el perfil `demo` (carga ciudadanos ficticios `9000000001`–`9000000019` con estados
   Requerido / No requerido) y un administrador inicial (`ADMIN_INITIAL_*`). Ver [`../../backend/README.md`](../../backend/README.md).
   El backend necesita **JDK 21 completo**.
2. Web:
   ```bash
   cd proyecto/web
   ./mvnw spring-boot:run           # perfil api, http://localhost:8081
   ```
3. Prueba de humo (no escribe datos; las credenciales van solo en la terminal):
   ```bash
   ADMIN_IDENTIFICACION=... ADMIN_CONTRASENA='...' ./scripts/prueba-humo.sh
   ```
   Debe terminar con `0 FALLA`. Si hay un usuario de fuerza pública, agrega `AGENTE_IDENTIFICACION` y `AGENTE_CONTRASENA`.
4. Ten a mano: la contraseña del administrador (no la escribas en las diapositivas) y una identificación nueva para
   registrar en vivo (6 a 10 dígitos que no existan, por ejemplo `7000000001`).

## Guion (≈ 8 minutos)

| Paso | Qué mostrar | Qué decir |
|---|---|---|
| 1 | Abrir `/ciudadanos` sin sesión → login | «Toda la web exige sesión; la seguridad la valida el backend.» |
| 2 | Login con contraseña incorrecta, luego la correcta | «El token queda en la sesión del servidor, nunca en el navegador.» |
| 3 | Panel: total de ciudadanos | «Los datos vienen de la API REST; la web no toca la base de datos.» |
| 4 | Consultar `9.000.000.001` | Detalle con los 10 campos |
| 5 | Listado: filtrar «Rivera», paginar | Paginación del backend (20 por página) |
| 6 | Registrar: enviar vacío → errores; escribir `12ab` en identificación | Validación en el navegador con mensajes en español |
| 7 | Registrar con datos válidos | Confirmación y fila resaltada |
| 8 | Registrar la misma identificación | «Ya existe…» (409 del backend) |
| 9 | Editar el registrado: cambiar estatura → Guardar | PUT a la API y «Cambios guardados» |
| 10 | Reportes: Ver PDF «Requerido», luego Descargar «No requerido» | El PDF lo genera el backend; la web lo solicita |
| 11 | Cerrar sesión y pulsar «Atrás» | No se ven datos: la página no queda en caché |
| 12 (opcional) | Detener el backend y abrir el listado | «No fue posible conectar» con Reintentar |

## Plan B: sin backend

```bash
SPRING_PROFILES_ACTIVE=mock ./mvnw spring-boot:run
```

Cualquier credencial entra. Datos ficticios `1000000001`–`1000000003` y aviso **DATOS SIMULADOS — PRUEBA** en todas
las pantallas. El reporte «Requerido» trae un PDF simulado y «No requerido» muestra el mensaje de sin resultados.
Lo registrado se pierde al reiniciar.

## Lista de verificación para la integración

- [ ] `API_BASE_URL` apunta al backend integrado (por defecto `http://localhost:8080`).
- [ ] Puertos libres: 8080 (backend) y 8081 (web).
- [ ] `./mvnw test` → 83 pruebas OK.
- [ ] `./scripts/prueba-humo.sh` contra el entorno integrado → 0 FALLA (con `ESCRITURA=1` si se permite crear un ciudadano ficticio).
- [ ] Con un usuario de fuerza pública: la web responde «no tiene permisos de administrador».
- [ ] Nombres de campos iguales en base de datos, backend, web y móvil (ver tabla en el README).
- [ ] Ningún `.env`, contraseña ni token en el repositorio (`git status` y `git diff --cached` antes de cada commit).

## Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| «No fue posible conectar con el servidor» en todas las pantallas | Backend apagado o `API_BASE_URL` incorrecta | Levantar el backend o corregir la variable |
| «Port 8081 was already in use» | Otra instancia de la web abierta | Cerrarla o usar `SERVER_PORT=8082` |
| El administrador ve «no tiene permisos de administrador» | El usuario no tiene el perfil `ADMINISTRADOR` | Usar el admin inicial del backend |
| El backend no compila (`EndPosTable`) | JDK más nuevo que 21 | Compilar el backend con JDK 21 |
| «Su sesión expiró» muy seguido | Token corto (15 min) | Marcar «Recordar contraseña» (8 h) |
