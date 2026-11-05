# Manual de usuario — app móvil de Consulta Ciudadano

Guía rápida para el personal de la fuerza pública que usa la app en campo.

## Registrarse (solo la primera vez)

1. En la pantalla de inicio de sesión, toca **Registrarme**.
2. Completa identificación, nombres, apellidos, fuerza pública, rango (aparece después de elegir la fuerza), ID Fuerza y correo electrónico.
3. Envía el formulario. Si el registro es aceptado, la contraseña llega por correo electrónico — la app nunca la muestra ni la guarda. Si la identificación no figura en la validación de Defensa, o ya existe un registro con esa identificación o correo, la app explica el motivo.

## Iniciar sesión

1. Ingresa tu identificación y la contraseña que recibiste por correo.
2. Activa **Recordar contraseña** si quieres que la app mantenga la sesión abierta la próxima vez que la abras. Con esta opción, el teléfono guarda de forma cifrada únicamente un token de acceso, nunca la contraseña. Si la dejas desactivada, deberás volver a iniciar sesión cada vez que cierres la app.
3. Si la sesión guardada venció, la app te lo indica y vuelve a pedir la contraseña.

## Consultar el estado de una cédula

1. Desde la pantalla de inicio, toca **Escanear cédula**.
2. Concede el permiso de cámara si la app lo solicita. Si lo niegas, puedes concederlo después desde los ajustes del sistema, o usar el ingreso manual mientras tanto.
3. Apunta la cámara al código de barras de la cédula, dentro del marco guía.
4. Si el código no se puede leer (mala luz, reflejo, código dañado), la app lo indica y te deja intentar de nuevo. En cualquier momento puedes tocar **Ingresar identificación manualmente** y escribir el número en vez de escanear.
5. La app muestra el resultado devuelto por el servidor:
   - **REQUERIDO** o **NO REQUERIDO**: estado conocido.
   - Aviso de **sin información**: no hay un registro de antecedentes para esa identificación. Esto no significa que la persona no esté requerida; si necesitas confirmarlo, hazlo por otro medio.
   - Mensaje de error: problema de conexión, servidor caído o respuesta inesperada. Puedes tocar **Reintentar**.
6. Desde el resultado puedes **Escanear otra cédula** para seguir consultando, o **Volver al inicio**.

## Cerrar sesión

Desde la pantalla de inicio, toca **Cerrar sesión**. Esto borra el token guardado en el teléfono, aunque hayas activado «Recordar contraseña».

## Preguntas frecuentes

- **¿La app decide si alguien está requerido?** No. La app solo envía la identificación al servidor y muestra el estado que este responde.
- **¿Dónde queda mi contraseña?** En ningún lugar del teléfono. Solo se guarda un token de sesión cifrado, y solo si activaste «Recordar contraseña».
- **¿Qué hago si el escáner nunca lee el código?** Usa el ingreso manual de identificación; está disponible siempre, incluso sin permiso de cámara.
