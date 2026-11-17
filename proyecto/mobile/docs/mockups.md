# Mockups de la app móvil

Bocetos de baja fidelidad de las cinco pantallas de la app, agrupados según los dos flujos de navegación (ver `src/navigation/tipos.ts`). Se hicieron antes de dar por terminada cada pantalla, como guía de qué debía mostrar cada estado; la implementación final puede variar en detalle visual pero respeta esta estructura.

## Flujo de acceso (sin sesión)

| Pantalla | Mockup | Qué muestra |
|---|---|---|
| Login | [mockups/login.svg](mockups/login.svg) | Identificación, contraseña, «Recordar contraseña», aviso de modo simulado, error de credenciales |
| Registro | [mockups/registro.svg](mockups/registro.svg) | Identificación, nombres, apellidos, fuerza y rango (rango depende de la fuerza), ID Fuerza, correo, aviso de que la contraseña llega por correo, y los tres estados posibles (pendiente, aceptado, rechazado) |

## Flujo principal (con sesión)

| Pantalla | Mockup | Qué muestra |
|---|---|---|
| Inicio | [mockups/inicio.svg](mockups/inicio.svg) | Acceso directo a escanear y a cerrar sesión |
| Escáner | [mockups/escaner.svg](mockups/escaner.svg) | Visor de cámara con marco guía, aviso de código ilegible, alternativa de ingreso manual y cancelar. (Los estados de permiso pendiente/denegado no están dibujados aquí; son el mismo patrón de aviso + botón de acción.) |
| Resultado | [mockups/resultado.svg](mockups/resultado.svg) | Insignia de REQUERIDO/NO REQUERIDO, aviso de «sin información» para el 404 `SIN_INFORMACION`, y las acciones para escanear otra cédula o volver al inicio |

## Decisiones de navegación reflejadas en los mockups

- Quien no tiene sesión solo puede ver Login y Registro; quien sí tiene sesión solo ve Inicio, Escáner y Resultado. React Navigation reemplaza el stack completo al cambiar de estado (no se puede "volver" al login con el botón atrás).
- El escáner nunca bloquea al usuario: si la cámara falla, el permiso se niega, o el código no se puede leer, siempre hay una salida (reintentar, ingresar manualmente, o cancelar).
- El resultado siempre deja escanear otra cédula sin tener que pasar por Inicio, pensado para el uso real en campo (varias consultas seguidas).
