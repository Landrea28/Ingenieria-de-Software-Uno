#!/usr/bin/env bash
# Prueba de humo de la web administrativa contra un entorno en ejecución (web + backend).
# Úsala el día de integración o antes de la sustentación. Requiere bash, curl y grep (en Windows: Git Bash).
#
# Variables (las credenciales NUNCA se escriben en este archivo):
#   ADMIN_IDENTIFICACION, ADMIN_CONTRASENA   obligatorias: un usuario ADMINISTRADOR
#   WEB_URL                                  opcional, por defecto http://localhost:8081
#   AGENTE_IDENTIFICACION, AGENTE_CONTRASENA opcionales: un usuario de fuerza pública (prueba de rol incorrecto)
#   ESCRITURA=1                              opcional: además registra y edita un ciudadano ficticio (6xxxxxxxxx)
#
# Ejemplo:
#   ADMIN_IDENTIFICACION=1012345678 ADMIN_CONTRASENA='...' ./scripts/prueba-humo.sh
set -u

W="${WEB_URL:-http://localhost:8081}"
: "${ADMIN_IDENTIFICACION:?Defina ADMIN_IDENTIFICACION}"
: "${ADMIN_CONTRASENA:?Defina ADMIN_CONTRASENA}"
C="$(mktemp)"; trap 'rm -f "$C"' EXIT
OK=0; FALLA=0

caso() { # caso ID "descripción" comando...
  local id=$1 desc=$2; shift 2
  if "$@" > /dev/null 2>&1; then printf '%s | OK    | %s\n' "$id" "$desc"; OK=$((OK + 1))
  else printf '%s | FALLA | %s\n' "$id" "$desc"; FALLA=$((FALLA + 1)); fi
}
pagina() { curl -s -b "$C" -c "$C" "$@"; }
redirige_a() { [ "$(curl -s -o /dev/null -w '%{redirect_url}' -b "$C" -c "$C" "${@:2}")" = "$W$1" ]; }
contiene() { pagina "${@:2}" | grep -qF -- "$1"; }
login() { curl -s -o /dev/null -b "$C" -c "$C" -X POST --data-urlencode "identificacion=$1" --data-urlencode "contrasena=$2" "$W/login"; }
post_y_ver() { # POST sin seguir la redirección y luego GET a la pantalla de destino
  local destino; destino=$(curl -s -o /dev/null -w '%{redirect_url}' -b "$C" -c "$C" -X POST -d "$2" "$3")
  [ -n "$destino" ] && pagina "$destino" | grep -qF -- "$1"; }
ID_NUEVO="6$(date +%s | tail -c 10)"
FORM="identificacion=$ID_NUEVO&nombres=Prueba+Humo&apellidos=Integracion&fechaNacimiento=1990-05-05&lugarNacimiento=Tunja&fechaExpedicion=2008-06-06&lugarExpedicion=Bogota&rh=%2B&grupoSanguineo=O&estatura=1.70"
con() { local campo=${1%%=*}; echo "$FORM" | sed -E "s/(^|&)$campo=[^&]*/\1$1/"; }

echo "Prueba de humo contra $W"
echo "## Permisos"
caso P01 "Sin sesión: /ciudadanos redirige al login"           redirige_a /login "$W/ciudadanos"
caso P02 "Sin sesión: /reportes/pdf redirige al login"          redirige_a /login "$W/reportes/pdf?estado=REQUERIDO"
caso P03 "Login sin datos muestra mensaje"                      contiene "Ingrese identificación y contraseña." -X POST -d "identificacion=&contrasena=" "$W/login"
if [ -n "${AGENTE_IDENTIFICACION:-}" ] && [ -n "${AGENTE_CONTRASENA:-}" ]; then
  caso P04 "Rol incorrecto (fuerza pública) no puede entrar"    contiene "no tiene permisos de administrador" -X POST --data-urlencode "identificacion=$AGENTE_IDENTIFICACION" --data-urlencode "contrasena=$AGENTE_CONTRASENA" "$W/login"
  caso P05 "Tras el rechazo por rol no queda sesión"            redirige_a /login "$W/ciudadanos"
else
  echo "P04 | OMITIDO | Rol incorrecto: defina AGENTE_IDENTIFICACION y AGENTE_CONTRASENA"
fi
login "$ADMIN_IDENTIFICACION" "$ADMIN_CONTRASENA"
caso P06 "Administrador entra al panel"                         contiene "Sesión: $ADMIN_IDENTIFICACION" "$W/"

echo "## Campos obligatorios y formatos (no guardan nada)"
caso F01 "Formulario vacío marca los 10 campos"                 bash -c "[ \$(curl -s -b '$C' -X POST '$W/ciudadanos' | grep -c 'class=\"campo con-error\"') -eq 10 ]"
caso F02 "Identificación con letras"                            contiene "Debe tener entre 6 y 10 dígitos" -X POST -d "$(con identificacion=12ab56)" "$W/ciudadanos"
caso F03 "Nombres con números"                                  contiene "Solo letras y espacios." -X POST -d "$(con nombres=Prueba+2)" "$W/ciudadanos"
caso F04 "Fecha futura"                                         contiene "No puede ser una fecha futura." -X POST -d "$(con fechaNacimiento=2099-01-01)" "$W/ciudadanos"
caso F05 "Expedición anterior al nacimiento"                    contiene "Debe ser posterior a la fecha de nacimiento." -X POST -d "$(con fechaExpedicion=1980-01-01)" "$W/ciudadanos"
caso F06 "Estatura fuera de rango"                              contiene "Debe estar entre 0.50 y 2.50 m" -X POST -d "$(con estatura=3.10)" "$W/ciudadanos"
caso F07 "Los datos escritos se conservan tras un error"        contiene 'value="Tunja"' -X POST -d "$(con estatura=9)" "$W/ciudadanos"

echo "## Consulta y reportes"
caso C01 "El listado carga desde la API"                        contiene "Mostrando" "$W/ciudadanos"
caso C02 "Identificación inválida en la consulta"               contiene "Identificación inválida" -L "$W/ciudadanos/consulta?identificacion=12ab"
caso C03 "Reporte REQUERIDO devuelve un PDF"                    bash -c "curl -s -b '$C' '$W/reportes/pdf?estado=REQUERIDO' | head -c 5 | grep -q '%PDF-'"
caso C04 "Estado de reporte inválido pide elegir uno"           redirige_a "/reportes?resultado=estado" "$W/reportes/pdf?estado=TODOS"

if [ "${ESCRITURA:-0}" = "1" ]; then
  echo "## Registro y edición (crea el ciudadano ficticio $ID_NUEVO)"
  caso R01 "Registro válido con confirmación"                   redirige_a "/ciudadanos?busqueda=$ID_NUEVO" -X POST -d "$FORM" "$W/ciudadanos"
  caso R02 "Identificación duplicada"                           contiene "Ya existe un ciudadano con esta identificación." -X POST -d "$FORM" "$W/ciudadanos"
  caso R03 "Detalle del registrado"                             contiene "Prueba Humo" "$W/ciudadanos/$ID_NUEVO"
  caso R04 "Edición con confirmación"                           post_y_ver "Cambios guardados" "$(con nombres=Prueba+Editada)" "$W/ciudadanos/$ID_NUEVO"
  caso R05 "No se puede cambiar la identificación"              post_y_ver "no se puede modificar" "$(con identificacion=9000000002)" "$W/ciudadanos/$ID_NUEVO"
else
  echo "R0x | OMITIDO | Registro y edición: ejecute con ESCRITURA=1 para incluirlos"
fi

echo "## Seguridad"
caso S01 "Pantallas con Cache-Control: no-store"                bash -c "curl -s -D - -o /dev/null -b '$C' '$W/ciudadanos' | grep -qi '^cache-control: no-store'"
caso S02 "El panel no se puede incrustar en otro sitio"         bash -c "curl -s -D - -o /dev/null -b '$C' '$W/' | grep -qi '^x-frame-options: DENY'"
caso S03 "Cerrar sesión lleva al login"                         redirige_a "/login?salida" -X POST "$W/logout"
caso S04 "La sesión cerrada ya no da acceso"                    redirige_a /login "$W/ciudadanos"

printf '\nTotal: %d OK, %d FALLA\n' "$OK" "$FALLA"
[ "$FALLA" -eq 0 ]
