# Decisiones y supuestos de la app móvil

| Id | Decisión o supuesto | Justificación |
|---|---|---|
| S-01 | React Native + Expo; Android es el dispositivo de prueba, el mismo código corre en iOS | Cumple el requisito de app móvil con un solo código base |
| S-02 | El token de sesión vive en memoria; con «Recordar contraseña» activo se persiste únicamente el token (nunca la contraseña) en `expo-secure-store` (Keychain/Keystore) | La contraseña nunca debe tocar el almacenamiento del dispositivo |
| S-03 | Un 401 con token enviado se interpreta como sesión vencida o revocada y vuelve al login; un 401 sin token (p. ej. credenciales malas) no dispara ese flujo | Evita confundir «credenciales inválidas» con «sesión vencida» |
| S-04 | El catálogo de fuerzas y rangos se carga desde `GET /api/catalogos/fuerzas-rangos`; el selector de rango depende de la fuerza elegida | El backend es la única fuente de verdad del catálogo |
| S-05 | El registro exitoso no implica cuenta activa: el estado (pendiente, aceptado, rechazado) lo decide el backend; la contraseña llega por correo y la app nunca la muestra ni la guarda | Coincide con el flujo de validación SIMULADA del backend |
| S-06 | El modo de API (`real` o `mock`) se elige por variable de entorno; en modo `mock` la interfaz muestra un aviso visible para no confundirlo con el backend real | Permite avanzar sin depender del backend sin ocultar que los datos son simulados |
| S-07 | El cliente HTTP tiene un tiempo de espera configurable y aborta la petición al superarlo, reportándolo como un error distinto de un fallo de red | Da mensajes específicos en vez de un error genérico |
| S-08 | Un 404 en `/api/antecedentes/{id}` significa que no hay información y nunca se interpreta como `NO_REQUERIDO` | Igual que el supuesto del backend: ausencia de dato no equivale a «no requerido» |
| S-09 | En el emulador Android el backend local se alcanza en `10.0.2.2`; en un dispositivo físico hay que usar la IP LAN del equipo | Limitación conocida del emulador, documentada para evitar perder tiempo depurando conectividad |
| S-10 | El código de barras de la cédula se interpreta como una cadena numérica de 6 a 10 dígitos (la identificación), admitiendo espacios, guiones o puntos como separadores; si el resultado no cumple ese formato se considera «código ilegible» y se ofrece ingreso manual | No se contó con cédulas físicas reales para probar; se validó con códigos de prueba ficticios que siguen el mismo patrón de identificación usado en todo el contrato (`^[0-9]{6,10}$`), ver `src/utils/codigoBarras.test.ts` |

## Validación del supuesto S-10

Antes de depender del escáner para la demostración se probó la extracción con datos de prueba ficticios (no una cédula real, que no estaba disponible):

- Identificación simple: `9000000001` → se acepta tal cual.
- Con separadores: `900-000-0001`, `900.000.0001`, `" 9000000001 "` → se acepta tras quitar separadores.
- Ruido no numérico: una URL de ejemplo (caso típico si el lector detecta un QR en vez de un código 1D) o letras antes de los dígitos → se rechaza como ilegible.
- Longitud fuera de 6–10 dígitos → se rechaza como ilegible.

Los tipos de símbolo habilitados en el escáner (`code128`, `pdf417`, `code39`, `code93`, `itf14`, `codabar`) son los formatos 1D más comunes para documentos de identificación; si en una prueba con una cédula física el tipo real resulta distinto, basta agregarlo a la lista en `EscanerScreen.tsx` sin cambiar la lógica de extracción.

Si en la práctica la lectura automática no resulta confiable con un código real, la pantalla de escaneo siempre ofrece el ingreso manual de la identificación como alternativa, con la misma validación que usa el resto de la app.
