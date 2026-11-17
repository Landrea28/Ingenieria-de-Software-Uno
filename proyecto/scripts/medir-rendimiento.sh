#!/usr/bin/env bash
# Mide los RNF de rendimiento (RNF-R1 y RNF-R2) contra la API real con los datos demo.
#
# Requisitos: backend con perfiles demo,dev y PostgreSQL en marcha; curl y awk.
# Uso (las credenciales solo en la sesión de terminal, nunca en el repositorio):
#   SMOKE_ADMIN_IDENTIFICACION=... SMOKE_ADMIN_PASSWORD=... bash proyecto/scripts/medir-rendimiento.sh
# Opcionales: SMOKE_API_BASE (por defecto http://127.0.0.1:8080) y MUESTRAS (por defecto 30).
set -euo pipefail

API_BASE="${SMOKE_API_BASE:-http://127.0.0.1:8080}"
ADMIN_ID="${SMOKE_ADMIN_IDENTIFICACION:?Define SMOKE_ADMIN_IDENTIFICACION solo en la sesion}"
ADMIN_PASSWORD="${SMOKE_ADMIN_PASSWORD:?Define SMOKE_ADMIN_PASSWORD solo en la sesion}"
MUESTRAS="${MUESTRAS:-30}"

TOKEN="$(curl -fsS -X POST "$API_BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d "{\"identificacion\":\"$ADMIN_ID\",\"contrasena\":\"$ADMIN_PASSWORD\",\"recordarContrasena\":false}" \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
[ -n "$TOKEN" ] || { echo "No se pudo iniciar sesion como administrador" >&2; exit 1; }

FALLOS=0

# medir <RNF> <descripcion> <ruta> <meta en segundos>
medir() {
  local rnf="$1" descripcion="$2" ruta="$3" meta="$4" tiempos codigo
  tiempos="$(mktemp)"
  curl -s -o /dev/null -H "Authorization: Bearer $TOKEN" "$API_BASE$ruta"   # calentamiento, no cuenta
  for _ in $(seq "$MUESTRAS"); do
    codigo="$(curl -s -o /dev/null -w '%{http_code} %{time_total}' -H "Authorization: Bearer $TOKEN" "$API_BASE$ruta")"
    [ "${codigo%% *}" = "200" ] || { echo "FALLA: $ruta respondio ${codigo%% *}" >&2; rm -f "$tiempos"; exit 1; }
    echo "${codigo##* }" >> "$tiempos"
  done
  sort -n "$tiempos" | awk -v rnf="$rnf" -v d="$descripcion" -v meta="$meta" '
    { t[NR] = $1; suma += $1 }
    END {
      p95 = t[int(NR * 0.95 + 0.999)]; if (p95 == "") p95 = t[NR]
      printf "| %s | %s | %d | %.0f ms | %.0f ms | %.0f ms | < %.0f ms | %s |\n",
        rnf, d, NR, suma / NR * 1000, p95 * 1000, t[NR] * 1000, meta * 1000, (t[NR] < meta ? "Cumple" : "No cumple")
      exit (t[NR] < meta ? 0 : 3)
    }' || FALLOS=$((FALLOS + 1))
  rm -f "$tiempos"
}

echo "| RNF | Operación | Muestras | Promedio | p95 | Máximo | Meta | Resultado |"
echo "|---|---|---|---|---|---|---|---|"
medir RNF-R1 "Consulta de ciudadano por identificación" "/api/ciudadanos/9000000003" 1
medir RNF-R1 "Consulta de antecedentes por identificación" "/api/antecedentes/9000000003" 1
medir RNF-R1 "Listado paginado de ciudadanos" "/api/ciudadanos?pagina=0&tamano=20" 1
medir RNF-R1 "Búsqueda de ciudadanos" "/api/ciudadanos?busqueda=900000001&pagina=0&tamano=20" 1
medir RNF-R2 "Reporte PDF (REQUERIDO)" "/api/reportes/ciudadanos?estado=REQUERIDO" 3
medir RNF-R2 "Reporte PDF (NO_REQUERIDO)" "/api/reportes/ciudadanos?estado=NO_REQUERIDO" 3

[ "$FALLOS" -eq 0 ] || { echo "$FALLOS medicion(es) superan la meta" >&2; exit 3; }
