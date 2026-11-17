# Manual de usuario — Web administrativa de Consulta Ciudadano

Guía rápida para el **administrador** que gestiona ciudadanos y solicita reportes.

## Ingresar

1. Abre la dirección de la aplicación web (en local: <http://localhost:8081>).
2. Escribe tu identificación y tu contraseña. Activa **Recordar contraseña** si quieres que la sesión dure hasta 8 horas;
   si no, se cierra a los 15 minutos.
3. Si ves «Su usuario no tiene permisos de administrador», tu cuenta es de la fuerza pública: usa la app móvil.
4. Si ves «Su sesión expiró», vuelve a ingresar. La web nunca guarda tu contraseña.

## Panel principal

Muestra cuántos ciudadanos hay registrados y accesos a **Ciudadanos**, **Registrar ciudadano** y **Reportes**.
Desde el panel también puedes **consultar por identificación** directamente.

## Consultar un ciudadano

- **Por identificación:** escribe el número en «Consultar por identificación» (puede llevar puntos, por ejemplo
  `9.000.000.001`) y pulsa **Consultar**. Se abre el detalle con sus 10 datos.
- **En el listado:** en **Ciudadanos** puedes filtrar por parte de la identificación, los nombres o los apellidos.
  El listado muestra 20 por página; usa **Anterior** y **Siguiente**. En cada fila, **Ver** abre el detalle y
  **Editar** abre el formulario.

## Registrar un ciudadano

1. Pulsa **Registrar ciudadano** y completa los 10 campos (todos son obligatorios):
   identificación, nombres, apellidos, fecha y lugar de nacimiento, fecha y lugar de expedición, grupo sanguíneo,
   RH y estatura en metros (por ejemplo `1.70`).
2. Si un dato no es válido, el mensaje aparece debajo del campo. Lo escrito no se pierde.
3. Al registrar verás un mensaje verde y el ciudadano resaltado en el listado.
4. Si la identificación ya existe, verás «Ya existe un ciudadano con esta identificación».

## Editar un ciudadano

1. Desde el detalle o el listado, pulsa **Editar**. El formulario aparece con los datos actuales.
2. La identificación **no se puede cambiar**.
3. Pulsa **Guardar cambios**. Volverás al detalle con «Cambios guardados para …». **Cancelar** vuelve al detalle sin guardar.

## Solicitar un reporte

1. Abre **Reportes** y elige **Requerido** o **No requerido**.
2. **Ver PDF** lo abre en el navegador; **Descargar PDF** lo guarda como archivo (`ciudadanos-requerido-AAAA-MM-DD.pdf`).
3. Si no hay ciudadanos con ese estado, verás «No hay ciudadanos con estado … No se generó el reporte».
4. El PDF contiene datos personales: guárdalo solo donde corresponda.

## Cerrar sesión

Pulsa **Cerrar sesión** en el menú. La sesión se revoca en el servidor; si usas «Atrás» después, la web te pide
ingresar de nuevo.

## Mensajes frecuentes

| Mensaje | Qué significa | Qué hacer |
|---|---|---|
| «No fue posible conectar con el servidor» | El servidor no responde | Espera unos minutos y pulsa **Reintentar** |
| «Su sesión expiró» | El tiempo de la sesión terminó | Ingresa de nuevo |
| «Revisa los campos marcados» | Hay datos inválidos en el formulario | Corrige los campos en rojo |
| «El servidor rechazó los datos: …» | El servidor encontró un dato inválido | Corrige el campo indicado |
| «DATOS SIMULADOS — PRUEBA» | La web está en modo de demostración | Los datos no son reales ni se guardan |

## Usuarios y perfiles

Abre **Usuarios y perfiles** en el menú. Tiene dos pestañas.

### Usuarios

- **Ver y filtrar:** el listado muestra la identificación, el nombre, el correo, el perfil y el estado de cada usuario.
  Puedes filtrar por perfil, por estado o por ambos.
- **Crear:** pulsa **Crear usuario** y escribe identificación, correo, nombres y apellidos, y elige el perfil. No se
  pide contraseña: el sistema genera una temporal y la envía al correo del usuario; nunca aparece en pantalla.
- **Editar:** pulsa **Editar** en la fila. Puedes cambiar los datos, el perfil y el estado. La identificación no se
  puede cambiar.
- **Retirar el acceso:** no existe «eliminar». Edita el usuario y pasa su estado a **Inactivo**; no podrá iniciar
  sesión. Para devolverle el acceso, vuelve a ponerlo en **Activo**.

Mensajes que puedes ver:

| Mensaje | Qué hacer |
|---|---|
| Ya existe un usuario con esta identificación. | Busca al usuario en el listado y edítalo |
| Este correo ya está registrado por otro usuario. | Usa otro correo |
| No puede desactivar su propia cuenta ni quitarse el perfil de administrador. | Pídele el cambio a otro administrador |
| Debe quedar al menos un administrador activo en el sistema. | Crea o activa otro administrador antes |

### Perfiles y permisos

1. Elige el perfil: **Administrador** o **Personal de fuerza pública**. Los perfiles son fijos; no se crean ni se borran.
2. Marca lo que ese perfil puede hacer en cada opción del sistema (ciudadanos, antecedentes, reportes y seguridad).
3. Pulsa **Guardar permisos**. El perfil queda exactamente con lo marcado.

El permiso **Administrar usuarios y perfiles** del perfil Administrador aparece bloqueado: no se puede quitar, para
que siempre haya quien administre el sistema.
