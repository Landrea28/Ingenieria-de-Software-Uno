# Prototipos de la web administrativa

Bocetos de baja fidelidad de las pantallas del **administrador**. Muestran qué debe tener cada pantalla y sus
estados (vacío, error, confirmación); la implementación final puede variar en detalle visual pero respeta esta
estructura. Las notas con ✎ son anotaciones del prototipo, no texto de la pantalla.

Todos los datos de los bocetos son ficticios.

## Acceso (sin sesión)

| Pantalla | Prototipo | Qué muestra | Implementada en |
|---|---|---|---|
| Login del administrador | [mockups/login.svg](mockups/login.svg) | Identificación, contraseña, «Recordar contraseña» y los mensajes de credenciales incorrectas, sesión expirada, sesión cerrada y usuario sin permisos de administrador | `templates/login.html` |

## Gestión de ciudadanos (con sesión)

| Pantalla | Prototipo | Qué muestra | Implementada en |
|---|---|---|---|
| Panel principal | [mockups/panel.svg](mockups/panel.svg) | Consulta rápida por identificación, total de ciudadanos y accesos a cada módulo | `templates/panel.html` |
| Listado y búsqueda | [mockups/listado.svg](mockups/listado.svg) | Consulta exacta por identificación, filtro parcial, tabla paginada (20 por página) y acciones Ver / Editar | `templates/ciudadanos/lista.html` |
| Registro | [mockups/registro.svg](mockups/registro.svg) | Los 10 campos agrupados (datos personales, documento, datos físicos), resumen de errores y mensaje junto a cada campo | `templates/ciudadanos/formulario.html` |
| Detalle y edición | [mockups/detalle-edicion.svg](mockups/detalle-edicion.svg) | Detalle de un ciudadano y el mismo formulario precargado, con la identificación bloqueada y la confirmación al guardar | `templates/ciudadanos/detalle.html` y `formulario.html` |
| Reportes | [mockups/reportes.svg](mockups/reportes.svg) | Filtro Requerido / No requerido, Ver PDF, Descargar PDF y el mensaje cuando no hay resultados | `templates/reportes.html` |

## Seguridad

| Pantalla | Prototipo | Qué muestra | Implementada en |
|---|---|---|---|
| Usuarios y perfiles | [mockups/usuarios-perfiles.svg](mockups/usuarios-perfiles.svg) | Listado de usuarios con filtro por perfil y estado, formulario de crear / editar, y permisos por perfil | `templates/usuarios/lista.html`, `usuarios/formulario.html` y `perfiles.html`. Se implementó en pantallas separadas (listado, formulario y pestaña de permisos) en lugar de todo en una, y los permisos se agrupan por opción del sistema |

## Decisiones reflejadas en los prototipos

- **Sin sesión no hay pantallas.** Cualquier ruta sin sesión, o cualquier respuesta 401/403 de la API, lleva al login.
  Ocultar opciones del menú no es la protección: quien autoriza es el backend con el token.
- **El menú lateral es el mismo en todas las pantallas** y siempre muestra la sesión activa y «Cerrar sesión».
- **Consultar y buscar son cosas distintas.** «Consultar por identificación» abre el detalle exacto de una persona;
  «Buscar» filtra el listado por parte de la identificación, los nombres o los apellidos.
- **Registro y edición comparten formulario.** Al editar, la identificación no se puede cambiar porque es la clave
  con la que el backend ubica al ciudadano.
- **Los errores se muestran junto al campo** y nunca borran lo que el administrador ya escribió.
- **El PDF no se arma en la web.** Lo genera el backend; la web solo lo entrega para verlo o descargarlo.
- **No se eliminan usuarios.** Se desactivan, para conservar el historial; por eso no hay botón «Eliminar».

Las capturas de las pantallas ya implementadas están en [evidencias/](evidencias/) y se listan en
[pruebas.md](pruebas.md#6-evidencias).
