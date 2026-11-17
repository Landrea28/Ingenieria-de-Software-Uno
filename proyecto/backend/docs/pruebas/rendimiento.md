# Medición de los RNF de rendimiento

Resultado de medir RNF-R1 y RNF-R2 de [requisitos.md](../requisitos.md) contra la API real con los datos demo.

## Método

- Script: [`proyecto/scripts/medir-rendimiento.sh`](../../../scripts/medir-rendimiento.sh).
- Cada operación se invoca 30 veces seguidas con `curl`, con un token de administrador, después de una petición de calentamiento que no cuenta.
- Se mide el tiempo total de cada petición (`time_total`: conexión, proceso en el servidor y descarga de la respuesta).
- El requisito se da por cumplido si **el máximo** de las 30 muestras queda por debajo de la meta.

## Entorno

- Fecha: 2026-11-16.
- Backend con perfiles `demo,dev`, PostgreSQL 17 en Docker, todo en el mismo equipo (red local).
- Datos demo: 20 ciudadanos y 19 antecedentes ficticios.
- Equipo: AMD Ryzen 9 5900HS (16 hilos), 15 GB de RAM, Fedora Linux, JDK 25.

## Resultados

| RNF | Operación | Muestras | Promedio | p95 | Máximo | Meta | Resultado |
|---|---|---|---|---|---|---|---|
| RNF-R1 | Consulta de ciudadano por identificación | 30 | 26 ms | 33 ms | 37 ms | < 1000 ms | Cumple |
| RNF-R1 | Consulta de antecedentes por identificación | 30 | 50 ms | 89 ms | 89 ms | < 1000 ms | Cumple |
| RNF-R1 | Listado paginado de ciudadanos | 30 | 64 ms | 124 ms | 124 ms | < 1000 ms | Cumple |
| RNF-R1 | Búsqueda de ciudadanos | 30 | 25 ms | 45 ms | 55 ms | < 1000 ms | Cumple |
| RNF-R2 | Reporte PDF (REQUERIDO) | 30 | 152 ms | 812 ms | 1591 ms | < 3000 ms | Cumple |
| RNF-R2 | Reporte PDF (NO_REQUERIDO) | 30 | 13 ms | 23 ms | 23 ms | < 3000 ms | Cumple |

RNF-R1 (consultas y listados en menos de 1 s) y RNF-R2 (PDF en menos de 3 s) **se cumplen** en el entorno local. El máximo del reporte de requeridos (1,6 s) corresponde a la primera generación, que carga las clases de la librería PDF; las siguientes tardan alrededor de 100 ms.

## Alcance y límites

- Es una medición en un solo equipo, con un usuario a la vez y 20 registros; no es una prueba de carga ni de concurrencia.
- Los tiempos cambian con el hardware. Para repetirla:

```bash
SMOKE_ADMIN_IDENTIFICACION=... SMOKE_ADMIN_PASSWORD=... bash proyecto/scripts/medir-rendimiento.sh
```

El script termina con código distinto de cero si alguna operación supera su meta.
