# Registro de supuestos y decisiones

Registro único de los supuestos y decisiones técnicas del proyecto. Reemplaza a los tres archivos que antes llevaba cada módulo por separado (backend, seguridad y móvil), cuyos identificadores se repetían. Cada entrada indica a qué se refiere, por qué se tomó y qué módulo la aplica; la última columna conserva el identificador anterior para poder rastrearla.

Los identificadores llevan un prefijo según el tema:

| Prefijo | Tema | De qué tratan |
|---|---|---|
| `SIM` | Simulaciones | Lo que el sistema simula porque no hay acceso a la fuente o al servicio real |
| `DAT` | Datos y API | Modelo de datos, contrato de la API y despliegue del backend |
| `SEG` | Seguridad | Contraseñas, sesiones, usuarios, auditoría y acceso |
| `MOV` | App móvil | Comportamiento del cliente móvil y lectura de la cédula |

## Simulaciones

Nada de lo que aparece en esta sección es una integración oficial. Todo está etiquetado como **SIMULADO** en el código, la API, los correos y la documentación.

| Id | Supuesto o decisión | Por qué | Módulo | Id anterior |
|---|---|---|---|---|
| SIM-01 | La validación del personal contra el Departamento de Defensa es simulada: el adaptador `ValidadorDefensa` consulta la tabla `defensa_personal_simulado` | No hay acceso a la fuente oficial y sin simulación no se puede demostrar el registro. La interfaz permite cambiarla por una integración real sin tocar controladores ni clientes | Backend · Seguridad | Seguridad S-01, Backend S-05 |
| SIM-02 | El correo con la contraseña se envía por SMTP configurable; en desarrollo se usa Mailpit como servidor de pruebas | Permite verificar la entrega y el contenido sin enviar credenciales a destinatarios reales | Backend · Seguridad | Seguridad S-02, Backend S-13 |
| SIM-03 | Todos los antecedentes, ciudadanos y personal de demostración son ficticios; la fuente del estado es `SIMULADA` | No hay fuente oficial de antecedentes y no se deben usar datos personales reales | Backend · Datos | Backend S-05 |
| SIM-04 | El perfil `demo` carga los datos ficticios y solo debe usarse sobre una base limpia | Evita mezclar datos de prueba con datos que se deban conservar | Backend · Datos | Backend S-09 |
| SIM-05 | `PUT /api/antecedentes/{id}/estado` existe solo para demostraciones | Permite cambiar un estado ficticio durante la demo; debe protegerse o retirarse antes de cualquier despliegue | Backend | Backend S-06 |
| SIM-06 | La app móvil puede apuntar a un servidor falso (`npm run mock`) elegido por variable de entorno; en ese modo la interfaz muestra un aviso visible | Permite avanzar sin el backend sin ocultar que los datos son simulados | Móvil | Móvil S-06 |

## Datos y API

| Id | Supuesto o decisión | Por qué | Módulo | Id anterior |
|---|---|---|---|---|
| DAT-01 | La identificación es un `String` de 6 a 10 dígitos | Evita perder ceros iniciales y unifica la validación en backend, web y móvil | Backend · Web · Móvil | Backend S-01 |
| DAT-02 | La identificación no se modifica al actualizar un ciudadano y no hay borrado de ciudadanos | Es la clave de negocio | Backend · Web | Backend S-02 |
| DAT-03 | El estado de antecedentes vive en una tabla propia, con un registro por ciudadano | Permite cambiar la fuente del estado sin tocar la tabla `ciudadano` | Backend · Datos | Backend S-03 |
| DAT-04 | Un ciudadano inexistente o sin estado responde 404 `SIN_INFORMACION`, nunca `NO_REQUERIDO` | La ausencia de dato no equivale a «no requerido»; evita falsos negativos | Backend · Móvil | Backend S-04, Móvil S-08 |
| DAT-05 | Si ningún ciudadano cumple el filtro, el reporte responde 404 `SIN_RESULTADOS` en lugar de un PDF vacío | Distingue una consulta válida sin datos de un reporte generado y permite que la web lo avise con claridad | Backend · Web | Backend S-11 |
| DAT-06 | El esquema se gestiona con Flyway; Hibernate solo lo valida | La base se reconstruye desde cero y cada cambio de esquema queda en una migración | Backend · Datos | Backend S-08 |
| DAT-07 | El servidor de aplicaciones es Tomcat embebido en Spring Boot (JAR ejecutable) | Cumple el requisito del enunciado y simplifica la instalación reproducible | Backend · Web | Backend S-07 |
| DAT-08 | El backend escucha en `127.0.0.1` por defecto | Limita la exposición en desarrollo; para probar desde un teléfono se cambia `SERVER_ADDRESS` de forma explícita | Backend | Backend S-10 |

## Seguridad

| Id | Supuesto o decisión | Por qué | Módulo | Id anterior |
|---|---|---|---|---|
| SEG-01 | La contraseña temporal tiene exactamente seis caracteres alfanuméricos, con al menos una mayúscula, una minúscula y un número | Es la regla literal del enunciado. La debilidad de una clave corta se compensa con hash BCrypt y bloqueo por intentos fallidos | Seguridad | Seguridad S-03, Backend S-14 |
| SEG-02 | «Recordar contraseña» alarga la vigencia del token; la contraseña nunca se recuerda ni se guarda | Guardar credenciales reversibles contradice la protección de contraseñas; el token cumple la misma función y puede expirar y revocarse | Seguridad · Móvil | Seguridad S-06, Backend S-15 |
| SEG-03 | Los usuarios no se eliminan; se marcan `INACTIVO` | Conserva la trazabilidad de auditoría y no rompe referencias históricas | Seguridad | Seguridad S-07 |
| SEG-04 | Siempre debe existir al menos un `ADMINISTRADOR` activo | Evita perder la capacidad de administrar accesos y permisos | Seguridad | Seguridad S-08 |
| SEG-05 | Los perfiles son fijos: el administrador consulta perfiles y administra sus permisos, pero no crea ni elimina perfiles | Mantiene estable el catálogo de roles que usa la autorización | Seguridad | Backend S-12 |
| SEG-06 | La auditoría registra identificación e IP, pero no contraseñas, hashes ni tokens completos | Permite investigar accesos sin convertir los registros en un depósito de credenciales | Seguridad | Seguridad S-09 |
| SEG-07 | CORS se configura con `CORS_ALLOWED_ORIGINS`, con orígenes explícitos y sin comodín | Permite distintos despliegues sin abrir el acceso desde cualquier origen | Seguridad | Seguridad S-10 |

## App móvil

| Id | Supuesto o decisión | Por qué | Módulo | Id anterior |
|---|---|---|---|---|
| MOV-01 | React Native con Expo; Android es el dispositivo de prueba y el mismo código corre en iOS | Cumple el requisito de app móvil con un solo código base | Móvil | Móvil S-01 |
| MOV-02 | El token de sesión vive en memoria; con «Recordar contraseña» se guarda únicamente el token en `expo-secure-store` (Keystore / Keychain) | La contraseña nunca debe tocar el almacenamiento del dispositivo (ver SEG-02) | Móvil | Móvil S-02 |
| MOV-03 | Un 401 con token enviado significa sesión vencida o revocada y devuelve al login; un 401 sin token (credenciales malas) no dispara ese flujo | Evita confundir «credenciales inválidas» con «sesión vencida» | Móvil | Móvil S-03 |
| MOV-04 | El catálogo de fuerzas y rangos se carga de `GET /api/catalogos/fuerzas-rangos`; el rango depende de la fuerza elegida | El backend es la única fuente de verdad del catálogo | Móvil | Móvil S-04 |
| MOV-05 | El resultado del registro (pendiente, aceptado o rechazado) lo decide el backend; la contraseña llega por correo y la app nunca la muestra ni la guarda | Coincide con el flujo de validación simulada del backend (SIM-01 y SIM-02) | Móvil | Móvil S-05 |
| MOV-06 | El cliente HTTP tiene un tiempo de espera configurable y lo reporta como un error distinto de un fallo de red | Da mensajes específicos en vez de un error genérico | Móvil | Móvil S-07 |
| MOV-07 | En el emulador Android el backend local se alcanza en `10.0.2.2`; en un teléfono se usa la IP del equipo en la red local | Limitación conocida del emulador, documentada para no perder tiempo depurando conectividad | Móvil | Móvil S-09 |
| MOV-08 | El código de barras de la cédula se interpreta como una cadena de 6 a 10 dígitos, admitiendo espacios, guiones o puntos como separadores; si no cumple, se trata como «código no legible» y se ofrece el ingreso manual | No se contó con cédulas reales; se validó con códigos de prueba ficticios que siguen el patrón de identificación de todo el contrato (DAT-01) | Móvil | Móvil S-10 |

### Validación de MOV-08

La extracción se probó con datos ficticios, no con una cédula real (ver `mobile/src/utils/codigoBarras.test.ts` y [codigos-prueba.md](../mobile/docs/codigos-prueba.md)):

- Identificación simple: `9000000001` se acepta tal cual.
- Con separadores: `900-000-0001`, `900.000.0001` y `" 9000000001 "` se aceptan tras quitar los separadores.
- Ruido no numérico (por ejemplo una URL leída de un QR) o letras antes de los dígitos: se rechaza como no legible.
- Longitud fuera de 6 a 10 dígitos: se rechaza como no legible.

Los tipos de símbolo habilitados en el escáner (`code128`, `pdf417`, `code39`, `code93`, `itf14` y `codabar`) cubren los formatos más comunes; si una cédula física usa otro, basta agregarlo a la lista de `EscanerScreen.tsx` sin cambiar la extracción. La lectura con cámara se comprobó en un teléfono físico con los códigos de prueba (casos M-10 a M-12 de [pruebas.md](../mobile/docs/pruebas.md)); con una cédula física real no se ha probado.
