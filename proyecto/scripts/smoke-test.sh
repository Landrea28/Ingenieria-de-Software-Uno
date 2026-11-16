#!/usr/bin/env bash
set -Eeuo pipefail

API_BASE="${SMOKE_API_BASE:-http://127.0.0.1:8080}"
MAILPIT_BASE="${SMOKE_MAILPIT_BASE:-http://127.0.0.1:8025}"
ADMIN_ID="${SMOKE_ADMIN_IDENTIFICACION:?Define SMOKE_ADMIN_IDENTIFICACION solo en la sesion}"
ADMIN_PASSWORD="${SMOKE_ADMIN_PASSWORD:?Define SMOKE_ADMIN_PASSWORD solo en la sesion}"
TMP_DIR="$(mktemp -d)"
LAST_STATUS=""
LAST_BODY="$TMP_DIR/body"
RESTORE_IDS=()
RESTORE_STATES=()
FAILURES=()

cleanup() {
    local index
    for index in "${!RESTORE_IDS[@]}"; do
        curl -sS -o /dev/null -X PUT \
            -H "Authorization: Bearer ${ADMIN_TOKEN:-}" \
            -H 'Content-Type: application/json' \
            --data "{\"estado\":\"${RESTORE_STATES[$index]}\"}" \
            "$API_BASE/api/antecedentes/${RESTORE_IDS[$index]}/estado" || true
    done
    rm -rf "$TMP_DIR"
}
trap cleanup EXIT

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    if [[ -s "$LAST_BODY" ]]; then
        printf 'Respuesta HTTP %s: ' "$LAST_STATUS" >&2
        jq -c . "$LAST_BODY" 2>/dev/null >&2 || head -c 500 "$LAST_BODY" >&2
        printf '\n' >&2
    fi
    exit 1
}

pass() {
    printf 'PASS: %s\n' "$1"
}

api() {
    local method="$1" path="$2" token="${3:-}" data="${4:-}"
    local args=(-sS -o "$LAST_BODY" -w '%{http_code}' -X "$method")
    if [[ -n "$token" ]]; then
        args+=(-H "Authorization: Bearer $token")
    fi
    if [[ -n "$data" ]]; then
        args+=(-H 'Content-Type: application/json' --data "$data")
    fi
    LAST_STATUS="$(curl "${args[@]}" "$API_BASE$path")" || fail "No se pudo conectar con $API_BASE$path"
}

expect_status() {
    [[ "$LAST_STATUS" == "$1" ]] || fail "$2 (esperado HTTP $1, recibido $LAST_STATUS)"
}

login() {
    local identification="$1" password="$2" remember="$3"
    local payload
    payload="$(jq -cn --arg id "$identification" --arg password "$password" \
        --argjson remember "$remember" \
        '{identificacion:$id,contrasena:$password,recordarContrasena:$remember}')"
    api POST /api/auth/login "" "$payload"
    expect_status 200 "Login de $identification"
    jq -er '.token' "$LAST_BODY" || fail "Login no devolvio token"
}

pdf_check() {
    local state="$1" file="$TMP_DIR/${state}.pdf"
    LAST_STATUS="$(curl -sS -D "$TMP_DIR/pdf.headers" -o "$file" -w '%{http_code}' \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        "$API_BASE/api/reportes/ciudadanos?estado=$state")" \
        || fail "No se pudo solicitar el PDF $state"
    [[ "$LAST_STATUS" == 200 ]] || fail "PDF $state (HTTP $LAST_STATUS)"
    grep -Eiq '^Content-Type:[[:space:]]*application/pdf([;[:space:]]|$)' "$TMP_DIR/pdf.headers" \
        || fail "PDF $state sin Content-Type application/pdf"
    [[ "$(head -c 4 "$file")" == '%PDF' ]] || fail "PDF $state sin cabecera %PDF"
    pass "PDF $state válido"
}

pdf_no_results_check() {
    local file="$TMP_DIR/REQUERIDO-empty.json"
    LAST_STATUS="$(curl -sS -D "$TMP_DIR/pdf-empty.headers" -o "$file" -w '%{http_code}' \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        "$API_BASE/api/reportes/ciudadanos?estado=REQUERIDO")" \
        || fail "No se pudo solicitar el PDF sin resultados"
    if [[ "$LAST_STATUS" == 404 ]] && [[ "$(jq -r '.codigo' "$file")" == "SIN_RESULTADOS" ]]; then
        pass "Filtro PDF REQUERIDO sin resultados: HTTP 404 SIN_RESULTADOS"
    else
        printf 'FAIL: PDF REQUERIDO sin resultados (esperado HTTP 404 SIN_RESULTADOS; recibido HTTP %s)\n' \
            "$LAST_STATUS" >&2
        if [[ -s "$file" ]]; then
            jq -c . "$file" 2>/dev/null >&2 || head -c 300 "$file" >&2
            printf '\n' >&2
        fi
        FAILURES+=("El reporte sin resultados no devuelve PDF con cabecera %PDF")
    fi
}

printf 'Smoke test contra %s\n' "$API_BASE"

api GET /api/health
expect_status 200 "Health"
[[ "$(jq -r '.estado' "$LAST_BODY")" == "OK" ]] || fail "Health no reporto OK"
pass "health 200"

ADMIN_TOKEN="$(login "$ADMIN_ID" "$ADMIN_PASSWORD" true)"
pass "login administrador"

NOW="$(date +%s)"
CITIZEN_ID="$((7000000000 + NOW % 99999999))"
while :; do
    api GET "/api/ciudadanos/$CITIZEN_ID" "$ADMIN_TOKEN"
    [[ "$LAST_STATUS" == 404 ]] && break
    CITIZEN_ID="$((7000000000 + (CITIZEN_ID - 7000000000 + 1) % 99999999))"
done
CITIZEN_JSON="$(jq -cn --arg id "$CITIZEN_ID" '{
    identificacion:$id,nombres:"Prueba Smoke",apellidos:"Ciudadano",
    fechaNacimiento:"1990-01-15",lugarNacimiento:"Bogota",
    fechaExpedicion:"2010-03-20",lugarExpedicion:"Medellin",
    rh:"+",grupoSanguineo:"O",estatura:1.75
}')"
api POST /api/ciudadanos "$ADMIN_TOKEN" "$CITIZEN_JSON"
expect_status 201 "Crear ciudadano con 10 campos"
api GET '/api/ciudadanos?pagina=0&tamano=100' "$ADMIN_TOKEN"
expect_status 200 "Listar ciudadanos"
jq -e --arg id "$CITIZEN_ID" '.content | any(.identificacion == $id)' "$LAST_BODY" >/dev/null \
    || fail "El ciudadano creado no aparece en el listado"
api GET "/api/ciudadanos?busqueda=$CITIZEN_ID&pagina=0&tamano=20" "$ADMIN_TOKEN"
expect_status 200 "Buscar ciudadano"
jq -e --arg id "$CITIZEN_ID" '.content | any(.identificacion == $id)' "$LAST_BODY" >/dev/null \
    || fail "La búsqueda no encuentra el ciudadano"
api PUT "/api/ciudadanos/$CITIZEN_ID" "$ADMIN_TOKEN" \
    "$(jq -cn '{nombres:"Prueba Smoke Actualizada",apellidos:"Ciudadano",
        fechaNacimiento:"1990-01-15",lugarNacimiento:"Bogota",
        fechaExpedicion:"2010-03-20",lugarExpedicion:"Medellin",
        rh:"+",grupoSanguineo:"O",estatura:1.75}')"
expect_status 200 "Actualizar ciudadano"
pass "ciudadano: crear (10 campos), listar, buscar y actualizar"

# Los identificadores y datos coinciden con la lista ficticia de Defensa V100/V101.
PERSONAL_CANDIDATES=(
    '8000000005|Ana|Prueba5|POLICIA|2|DEMO-005'
    '8000000006|Luis|Prueba6|EJERCITO|5|DEMO-006'
    '8000000007|Mia|Prueba7|ARMADA|8|DEMO-007'
    '8000000008|Juan|Prueba8|FUERZA_AEREA|11|DEMO-008'
    '8000000009|Eva|Prueba9|POLICIA|3|DEMO-009'
    '8000000010|Leo|Prueba10|EJERCITO|6|DEMO-010'
)
PERSONAL_ID=""
for candidate in "${PERSONAL_CANDIDATES[@]}"; do
    IFS='|' read -r candidate_id _ <<<"$candidate"
    api GET "/api/usuarios/$candidate_id" "$ADMIN_TOKEN"
    if [[ "$LAST_STATUS" == 404 ]]; then
        IFS='|' read -r PERSONAL_ID PERSONAL_NAME PERSONAL_SURNAME FORCE_CODE RANK_ID FORCE_ID <<<"$candidate"
        break
    fi
done
[[ -n "$PERSONAL_ID" ]] || fail "No quedan identidades ficticias de Defensa libres para registrar"
PERSONAL_EMAIL="smoke-${PERSONAL_ID}@example.test"
PERSONAL_JSON="$(jq -cn --arg id "$PERSONAL_ID" --arg names "$PERSONAL_NAME" \
    --arg surnames "$PERSONAL_SURNAME" --arg force "$FORCE_CODE" \
    --arg forceId "$FORCE_ID" --arg email "$PERSONAL_EMAIL" --argjson rank "$RANK_ID" \
    '{identificacion:$id,nombres:$names,apellidos:$surnames,fuerzaCodigo:$force,
      rangoId:$rank,idFuerza:$forceId,correo:$email}')"
api POST /api/personal/registro "" "$PERSONAL_JSON"
expect_status 201 "Registro de personal (Defensa SIMULADO)"
pass "registro personal validado con Defensa SIMULADO"

MESSAGE_ID=""
for _ in $(seq 1 30); do
    curl -fsS "$MAILPIT_BASE/api/v1/messages" -o "$TMP_DIR/messages.json" \
        || fail "No se pudo leer la API de Mailpit"
    MESSAGE_ID="$(jq -r --arg email "$PERSONAL_EMAIL" '
      .messages[]?
      | select(((.To // .to // []) | tostring | ascii_downcase | contains($email | ascii_downcase)))
      | (.ID // .Id // .id // empty)' "$TMP_DIR/messages.json" | head -n 1)"
    [[ -n "$MESSAGE_ID" ]] && break
    sleep 1
done
[[ -n "$MESSAGE_ID" ]] || fail "No llego a Mailpit el correo de $PERSONAL_EMAIL"
curl -fsS "$MAILPIT_BASE/api/v1/message/$MESSAGE_ID" -o "$TMP_DIR/message.json" \
    || fail "No se pudo leer el mensaje de Mailpit"
PERSONAL_PASSWORD="$(jq -r '[.. | strings] | join("\n")' "$TMP_DIR/message.json" \
    | python3 -c 'import re,sys; m=re.search(r"contraseña temporal es:\s*([A-Za-z0-9]{6})",sys.stdin.read(),re.I); print(m.group(1) if m else "")')"
[[ ${#PERSONAL_PASSWORD} -eq 6 ]] || fail "La contraseña del correo no tiene exactamente 6 caracteres"
[[ "$PERSONAL_PASSWORD" =~ [A-Z] && "$PERSONAL_PASSWORD" =~ [a-z] && "$PERSONAL_PASSWORD" =~ [0-9] ]] \
    || fail "La contraseña del correo no contiene mayúscula, minúscula y número"
pass "contraseña extraída de Mailpit: 6 caracteres con mayúscula, minúscula y número"

PERSONAL_TOKEN_SHORT="$(login "$PERSONAL_ID" "$PERSONAL_PASSWORD" false)"
SHORT_TTL="$(jq -er '.expiraEnSegundos' "$LAST_BODY")"
PERSONAL_TOKEN="$(login "$PERSONAL_ID" "$PERSONAL_PASSWORD" true)"
LONG_TTL="$(jq -er '.expiraEnSegundos' "$LAST_BODY")"
(( LONG_TTL > SHORT_TTL )) || fail "recordarContrasena=true no extiende el TTL"
pass "login personal con recordarContrasena=true y false"

api GET /api/usuarios "$PERSONAL_TOKEN"
expect_status 403 "PERSONAL en ruta administrativa"
api GET /api/perfiles "$PERSONAL_TOKEN"
expect_status 403 "PERSONAL en ruta de perfiles"
api GET /api/permisos "$PERSONAL_TOKEN"
expect_status 403 "PERSONAL en ruta de permisos"
pass "PERSONAL recibe 403 en rutas admin de usuarios, perfiles y permisos"

for state in REQUERIDO NO_REQUERIDO; do
    api GET "/api/antecedentes/9000000003" "$PERSONAL_TOKEN"
    if [[ "$state" == "NO_REQUERIDO" ]]; then
        api GET "/api/antecedentes/9000000001" "$PERSONAL_TOKEN"
    fi
    expect_status 200 "Consulta antecedentes $state"
    [[ "$(jq -r '.estado' "$LAST_BODY")" == "$state" ]] || fail "Estado de antecedente distinto a $state"
done
api GET /api/antecedentes/9000000020 "$PERSONAL_TOKEN"
expect_status 404 "Antecedente sin informacion"
pass "antecedentes REQUERIDO, NO_REQUERIDO y 404"

pdf_check REQUERIDO
pdf_check NO_REQUERIDO

# Vacía temporalmente el filtro REQUERIDO y restaura cada valor incluso si el test falla.
for id in 9000000003 9000000006 9000000009 9000000012 9000000015 9000000018; do
    api GET "/api/antecedentes/$id" "$ADMIN_TOKEN"
    if [[ "$LAST_STATUS" == 200 && "$(jq -r '.estado' "$LAST_BODY")" == "REQUERIDO" ]]; then
        RESTORE_IDS+=("$id")
        RESTORE_STATES+=(REQUERIDO)
        api PUT "/api/antecedentes/$id/estado" "$ADMIN_TOKEN" '{"estado":"NO_REQUERIDO"}'
        expect_status 200 "Preparar PDF sin resultados"
    fi
done
pdf_no_results_check
for index in "${!RESTORE_IDS[@]}"; do
    api PUT "/api/antecedentes/${RESTORE_IDS[$index]}/estado" "$ADMIN_TOKEN" \
        "{\"estado\":\"${RESTORE_STATES[$index]}\"}"
    expect_status 200 "Restaurar estado de antecedente"
done
RESTORE_IDS=()
RESTORE_STATES=()
pass "Se verificaron ambos filtros PDF y se solicitó el caso sin resultados"

api GET /api/usuarios "$ADMIN_TOKEN"
expect_status 200 "Listar usuarios"
jq -e '(.content // []) | all((has("passwordHash") or has("contrasena")) | not)' "$LAST_BODY" >/dev/null \
    || fail "La lista de usuarios expone un hash o una contraseña"
api GET /api/perfiles "$ADMIN_TOKEN"
expect_status 200 "Listar perfiles"
ADMIN_PROFILE_ID="$(jq -er '.[] | select(.codigo=="ADMINISTRADOR") | .id' "$LAST_BODY" | head -n 1)"
[[ -n "$ADMIN_PROFILE_ID" ]] || fail "No existe perfil ADMINISTRADOR"
api GET "/api/perfiles/$ADMIN_PROFILE_ID/permisos" "$ADMIN_TOKEN"
expect_status 200 "Consultar permisos del perfil"
PERMISSION_IDS="$(jq -c '[.[].id]' "$LAST_BODY")"
api PUT "/api/perfiles/$ADMIN_PROFILE_ID/permisos" "$ADMIN_TOKEN" \
    "$(jq -cn --argjson ids "$PERMISSION_IDS" '{permisosIds:$ids}')"
expect_status 200 "Actualizar permisos del perfil"
pass "perfiles: lectura y permisos"

USER_ID="81$(date +%s | tail -c 7)"
USER_EMAIL="smoke-${USER_ID}@example.test"
USER_JSON="$(jq -cn --arg id "$USER_ID" --arg email "$USER_EMAIL" \
    --argjson profile "$ADMIN_PROFILE_ID" \
    '{identificacion:$id,nombres:"Usuario Smoke",apellidos:"Temporal",correo:$email,perfilId:$profile}')"
api POST /api/usuarios "$ADMIN_TOKEN" "$USER_JSON"
expect_status 201 "Crear usuario admin"
jq -e 'has("passwordHash")|not' "$LAST_BODY" >/dev/null || fail "La respuesta de usuario expone passwordHash"
api GET "/api/usuarios/$USER_ID" "$ADMIN_TOKEN"
expect_status 200 "Leer usuario creado"
api PUT "/api/usuarios/$USER_ID" "$ADMIN_TOKEN" \
    "$(jq -cn --arg email "$USER_EMAIL" --argjson profile "$ADMIN_PROFILE_ID" \
      '{nombres:"Usuario Smoke Actualizado",apellidos:"Temporal",correo:$email,
        perfilId:$profile,estado:"INACTIVO"}')"
expect_status 200 "Actualizar usuario"
api PUT "/api/usuarios/$USER_ID" "$ADMIN_TOKEN" \
    "$(jq -cn --arg email "$USER_EMAIL" --argjson profile "$ADMIN_PROFILE_ID" \
      '{nombres:"Usuario Smoke Actualizado",apellidos:"Temporal",correo:$email,
        perfilId:$profile,estado:"ACTIVO"}')"
expect_status 200 "Reactivar usuario"
pass "usuarios admin: crear, leer, actualizar y comprobar ausencia de passwordHash"

api GET /api/usuarios
expect_status 401 "Ruta admin sin token"
api GET /api/usuarios "token-invalido"
expect_status 401 "Ruta admin con token invalido"
pass "rutas admin: 401 sin token y con token invalido"

api POST /api/auth/login "" \
    "$(jq -cn --arg id "$ADMIN_ID" '{identificacion:$id,contrasena:"Incorrecta-1",recordarContrasena:false}')"
expect_status 401 "Credenciales incorrectas"
KNOWN_ERROR="$(jq -r '.mensaje' "$LAST_BODY")"
api POST /api/auth/login "" \
    '{"identificacion":"8999999987","contrasena":"Incorrecta-1","recordarContrasena":false}'
expect_status 401 "Credenciales de identidad inexistente"
[[ "$KNOWN_ERROR" == "Credenciales inválidas" && "$(jq -r '.mensaje' "$LAST_BODY")" == "$KNOWN_ERROR" ]] \
    || fail "El mensaje de credenciales no es genérico"
pass "credenciales incorrectas: 401 y mensaje genérico"

for attempt in {1..6}; do
    api POST /api/auth/login "" \
        "$(jq -cn --arg id "$PERSONAL_ID" '{identificacion:$id,contrasena:"Wrong-1",recordarContrasena:false}')"
    expect_status 401 "Intento fallido $attempt de bloqueo"
done
api POST /api/auth/login "" \
    "$(jq -cn --arg id "$PERSONAL_ID" --arg password "$PERSONAL_PASSWORD" \
        '{identificacion:$id,contrasena:$password,recordarContrasena:false}')"
expect_status 401 "Login correcto debe estar bloqueado tras seis fallos"
pass "bloqueo de login tras 6 fallos (prueba de contraseña correcta también bloqueada)"

api POST /api/auth/logout "$ADMIN_TOKEN"
expect_status 204 "Logout administrador"
api GET /api/usuarios "$ADMIN_TOKEN"
expect_status 401 "Token revocado tras logout"
pass "logout y revocación del token (401)"

if ((${#FAILURES[@]} > 0)); then
    printf 'RESULTADO: %d comprobacion(es) fallaron:\n' "${#FAILURES[@]}" >&2
    printf ' - %s\n' "${FAILURES[@]}" >&2
    exit 1
fi
printf 'RESULTADO: todas las comprobaciones del smoke test finalizaron correctamente.\n'
