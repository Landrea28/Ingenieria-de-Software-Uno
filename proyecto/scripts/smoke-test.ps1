$ErrorActionPreference = 'Stop'

$ApiBase = if ($env:SMOKE_API_BASE) { $env:SMOKE_API_BASE } else { 'http://127.0.0.1:8080' }
$MailpitBase = if ($env:SMOKE_MAILPIT_BASE) { $env:SMOKE_MAILPIT_BASE } else { 'http://127.0.0.1:8025' }
$AdminId = $env:SMOKE_ADMIN_IDENTIFICACION
$AdminPassword = $env:SMOKE_ADMIN_PASSWORD
if (-not $AdminId -or -not $AdminPassword) {
    throw 'Define SMOKE_ADMIN_IDENTIFICACION y SMOKE_ADMIN_PASSWORD solo en esta sesión.'
}

$Client = [System.Net.Http.HttpClient]::new()
$script:Status = 0
$script:Body = ''
$script:Bytes = [byte[]]@()
$script:ContentType = ''
$script:AdminToken = ''
$Failures = [System.Collections.Generic.List[string]]::new()
$Restore = [System.Collections.Generic.List[object]]::new()

function Write-Pass([string]$Message) {
    Write-Output "PASS: $Message"
}

function Invoke-Api([string]$Method, [string]$Path, [string]$Token = '', [object]$Body = $null) {
    $Request = [System.Net.Http.HttpRequestMessage]::new(
        [System.Net.Http.HttpMethod]::new($Method), "$ApiBase$Path")
    if ($Token) {
        [void]$Request.Headers.TryAddWithoutValidation('Authorization', "Bearer $Token")
    }
    if ($null -ne $Body) {
        $Json = if ($Body -is [string]) { $Body } else { ConvertTo-Json -InputObject $Body -Depth 20 -Compress }
        $Request.Content = [System.Net.Http.StringContent]::new(
            $Json, [System.Text.Encoding]::UTF8, 'application/json')
    }
    $Response = $Client.SendAsync($Request).GetAwaiter().GetResult()
    $script:Status = [int]$Response.StatusCode
    $script:Bytes = $Response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
    $script:Body = [System.Text.Encoding]::UTF8.GetString($script:Bytes)
    $script:ContentType = if ($Response.Content.Headers.ContentType) {
        $Response.Content.Headers.ContentType.MediaType
    } else { '' }
    $Response.Dispose()
    $Request.Dispose()
}

function Assert-Status([int]$Expected, [string]$Action) {
    if ($script:Status -ne $Expected) {
        throw "$Action`: esperado HTTP $Expected, recibido $script:Status. $script:Body"
    }
}

function Get-Json {
    return $script:Body | ConvertFrom-Json
}

function Login([string]$Id, [string]$Password, [bool]$Remember) {
    Invoke-Api POST '/api/auth/login' '' @{
        identificacion = $Id
        contrasena = $Password
        recordarContrasena = $Remember
    }
    Assert-Status 200 "Login de $Id"
    $Result = Get-Json
    if (-not $Result.token) { throw 'El login no devolvio token.' }
    return $Result
}

function Invoke-Pdf([string]$State, [string]$Path) {
    $Request = [System.Net.Http.HttpRequestMessage]::new(
        [System.Net.Http.HttpMethod]::Get, "$ApiBase/api/reportes/ciudadanos?estado=$State")
    [void]$Request.Headers.TryAddWithoutValidation('Authorization', "Bearer $script:AdminToken")
    $Response = $Client.SendAsync($Request).GetAwaiter().GetResult()
    $script:Status = [int]$Response.StatusCode
    $script:ContentType = if ($Response.Content.Headers.ContentType) {
        $Response.Content.Headers.ContentType.MediaType
    } else { '' }
    $script:Bytes = $Response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
    $script:Body = [System.Text.Encoding]::UTF8.GetString($script:Bytes)
    if ($script:Status -eq 200) { [System.IO.File]::WriteAllBytes($Path, $script:Bytes) }
    $Response.Dispose()
    $Request.Dispose()
}

function Assert-Pdf([string]$State) {
    $Path = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), "smoke-$State.pdf")
    try {
        Invoke-Pdf $State $Path
        Assert-Status 200 "PDF $State"
        if ($script:ContentType -ne 'application/pdf' -or
            $script:Bytes.Length -lt 4 -or
            [System.Text.Encoding]::ASCII.GetString($script:Bytes, 0, 4) -ne '%PDF') {
            throw "PDF $State no tiene Content-Type application/pdf y cabecera %PDF."
        }
    } finally {
        if (Test-Path -LiteralPath $Path) { Remove-Item -LiteralPath $Path -Force }
    }
    Write-Pass "PDF $State valido"
}

function Restore-Antecedents {
    foreach ($Item in $Restore) {
        try {
            Invoke-Api PUT "/api/antecedentes/$($Item.Id)/estado" $script:AdminToken @{
                estado = $Item.State
            }
        } catch {
            Write-Warning "No se pudo restaurar el antecedente $($Item.Id): $_"
        }
    }
}

try {
    Write-Output "Smoke test contra $ApiBase"
    Invoke-Api GET '/api/health'
    Assert-Status 200 'Health'
    if ((Get-Json).estado -ne 'OK') { throw 'Health no reporto OK.' }
    Write-Pass 'health 200'

    $Admin = Login $AdminId $AdminPassword $true
    $script:AdminToken = $Admin.token
    Write-Pass 'login administrador'

    $CitizenId = [string](7000000000 + (([DateTimeOffset]::UtcNow.ToUnixTimeSeconds()) % 99999999))
    do {
        Invoke-Api GET "/api/ciudadanos/$CitizenId" $script:AdminToken
        if ($script:Status -ne 404) { $CitizenId = [string]([long]$CitizenId + 1) }
    } while ($script:Status -ne 404)
    Invoke-Api POST '/api/ciudadanos' $script:AdminToken @{
        identificacion = $CitizenId; nombres = 'Prueba Smoke'; apellidos = 'Ciudadano'
        fechaNacimiento = '1990-01-15'; lugarNacimiento = 'Bogota'
        fechaExpedicion = '2010-03-20'; lugarExpedicion = 'Medellin'
        rh = '+'; grupoSanguineo = 'O'; estatura = 1.75
    }
    Assert-Status 201 'Crear ciudadano de 10 campos'
    Invoke-Api GET '/api/ciudadanos?pagina=0&tamano=100' $script:AdminToken
    Assert-Status 200 'Listar ciudadanos'
    if (-not ((Get-Json).content | Where-Object identificacion -EQ $CitizenId)) {
        throw 'El ciudadano creado no aparece en el listado.'
    }
    Invoke-Api GET "/api/ciudadanos?busqueda=$CitizenId&pagina=0&tamano=20" $script:AdminToken
    Assert-Status 200 'Buscar ciudadano'
    Invoke-Api PUT "/api/ciudadanos/$CitizenId" $script:AdminToken @{
        nombres = 'Prueba Smoke Actualizada'; apellidos = 'Ciudadano'
        fechaNacimiento = '1990-01-15'; lugarNacimiento = 'Bogota'
        fechaExpedicion = '2010-03-20'; lugarExpedicion = 'Medellin'
        rh = '+'; grupoSanguineo = 'O'; estatura = 1.75
    }
    Assert-Status 200 'Actualizar ciudadano'
    Write-Pass 'ciudadano: crear (10 campos), listar, buscar y actualizar'

    $Candidates = @(
        @{ Id='8000000005'; Names='Ana'; Surname='Prueba5'; Force='POLICIA'; Rank=2; ForceId='DEMO-005' },
        @{ Id='8000000006'; Names='Luis'; Surname='Prueba6'; Force='EJERCITO'; Rank=5; ForceId='DEMO-006' },
        @{ Id='8000000007'; Names='Mia'; Surname='Prueba7'; Force='ARMADA'; Rank=8; ForceId='DEMO-007' },
        @{ Id='8000000008'; Names='Juan'; Surname='Prueba8'; Force='FUERZA_AEREA'; Rank=11; ForceId='DEMO-008' },
        @{ Id='8000000009'; Names='Eva'; Surname='Prueba9'; Force='POLICIA'; Rank=3; ForceId='DEMO-009' },
        @{ Id='8000000010'; Names='Leo'; Surname='Prueba10'; Force='EJERCITO'; Rank=6; ForceId='DEMO-010' }
    )
    $Personal = $null
    foreach ($Candidate in $Candidates) {
        Invoke-Api GET "/api/usuarios/$($Candidate.Id)" $script:AdminToken
        if ($script:Status -eq 404) { $Personal = $Candidate; break }
    }
    if (-not $Personal) { throw 'No quedan identidades ficticias de Defensa disponibles.' }
    $PersonalEmail = "smoke-$($Personal.Id)@example.test"
    Invoke-Api POST '/api/personal/registro' '' @{
        identificacion = $Personal.Id; nombres = $Personal.Names; apellidos = $Personal.Surname
        fuerzaCodigo = $Personal.Force; rangoId = $Personal.Rank
        idFuerza = $Personal.ForceId; correo = $PersonalEmail
    }
    Assert-Status 201 'Registro de personal (Defensa SIMULADO)'
    Write-Pass 'registro personal validado con Defensa SIMULADO'

    $Message = $null
    for ($i = 0; $i -lt 30 -and -not $Message; $i++) {
        $MessageList = Invoke-RestMethod "$MailpitBase/api/v1/messages"
        $Message = $MessageList.messages | Where-Object {
            (ConvertTo-Json $_ -Depth 20 -Compress) -match [regex]::Escape($PersonalEmail)
        } | Select-Object -First 1
        if (-not $Message) { Start-Sleep -Seconds 1 }
    }
    if (-not $Message) { throw "No llego a Mailpit el correo de $PersonalEmail." }
    $MessageId = if ($Message.ID) { $Message.ID } elseif ($Message.Id) { $Message.Id } else { $Message.id }
    $Mail = Invoke-RestMethod "$MailpitBase/api/v1/message/$MessageId"
    $MailJson = ConvertTo-Json $Mail -Depth 30 -Compress
    $Match = [regex]::Match($MailJson, 'contraseña temporal es:\s*([A-Za-z0-9]{6})', 'IgnoreCase')
    if (-not $Match.Success) { throw 'No se extrajo la contraseña temporal de seis caracteres del correo.' }
    $PersonalPassword = $Match.Groups[1].Value
    if ($PersonalPassword -cnotmatch '[A-Z]' -or $PersonalPassword -cnotmatch '[a-z]' -or
        $PersonalPassword -notmatch '[0-9]') { throw 'La contraseña del correo no cumple mayuscula/minuscula/numero.' }
    Write-Pass 'contraseña extraida de Mailpit: 6 caracteres con mayuscula, minuscula y numero'

    $Short = Login $Personal.Id $PersonalPassword $false
    $Long = Login $Personal.Id $PersonalPassword $true
    if ($Long.expiraEnSegundos -le $Short.expiraEnSegundos) {
        throw 'recordarContrasena=true no extiende el TTL.'
    }
    $PersonalToken = $Long.token
    Write-Pass 'login personal con recordarContrasena=true y false'

    Invoke-Api GET '/api/usuarios' $PersonalToken
    Assert-Status 403 'PERSONAL en ruta administrativa'
    Invoke-Api GET '/api/perfiles' $PersonalToken
    Assert-Status 403 'PERSONAL en ruta de perfiles'
    Invoke-Api GET '/api/permisos' $PersonalToken
    Assert-Status 403 'PERSONAL en ruta de permisos'
    Write-Pass 'PERSONAL recibe 403 en rutas admin de usuarios, perfiles y permisos'
    foreach ($ExpectedState in @('REQUERIDO', 'NO_REQUERIDO')) {
        $Id = if ($ExpectedState -eq 'REQUERIDO') { '9000000003' } else { '9000000001' }
        Invoke-Api GET "/api/antecedentes/$Id" $PersonalToken
        Assert-Status 200 "Consulta antecedentes $ExpectedState"
        if ((Get-Json).estado -ne $ExpectedState) { throw "Antecedente distinto a $ExpectedState." }
    }
    Invoke-Api GET '/api/antecedentes/9000000020' $PersonalToken
    Assert-Status 404 'Antecedente sin informacion'
    Write-Pass 'antecedentes REQUERIDO, NO_REQUERIDO y 404'

    Assert-Pdf REQUERIDO
    Assert-Pdf NO_REQUERIDO
    try {
        foreach ($Id in @('9000000003','9000000006','9000000009','9000000012','9000000015','9000000018')) {
            Invoke-Api GET "/api/antecedentes/$Id" $script:AdminToken
            if ($script:Status -eq 200 -and (Get-Json).estado -eq 'REQUERIDO') {
                $Restore.Add(@{ Id = $Id; State = 'REQUERIDO' })
                Invoke-Api PUT "/api/antecedentes/$Id/estado" $script:AdminToken @{ estado='NO_REQUERIDO' }
                Assert-Status 200 'Preparar PDF sin resultados'
            }
        }
        $EmptyPdfPath = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), 'smoke-empty.json')
        Invoke-Pdf REQUERIDO $EmptyPdfPath
        $EmptyResult = try { $script:Body | ConvertFrom-Json } catch { $null }
        if ($script:Status -eq 404 -and $EmptyResult.codigo -eq 'SIN_RESULTADOS') {
            Write-Pass 'Filtro PDF REQUERIDO sin resultados: HTTP 404 SIN_RESULTADOS'
        } else {
            $Failures.Add("Filtro PDF sin resultados: esperado HTTP 404 SIN_RESULTADOS; recibido HTTP $script:Status ($script:Body)")
            Write-Output "FAIL: $($Failures[$Failures.Count - 1])"
        }
    } finally {
        if ($EmptyPdfPath -and (Test-Path -LiteralPath $EmptyPdfPath)) {
            Remove-Item -LiteralPath $EmptyPdfPath -Force
        }
        Restore-Antecedents
        $Restore.Clear()
    }

    Invoke-Api GET '/api/usuarios' $script:AdminToken
    Assert-Status 200 'Listar usuarios'
    if ((Get-Json).content | Where-Object { $_.PSObject.Properties.Name -contains 'passwordHash' -or
            $_.PSObject.Properties.Name -contains 'contrasena' }) {
        throw 'La respuesta de usuarios expone hash o contraseña.'
    }
    Invoke-Api GET '/api/perfiles' $script:AdminToken
    Assert-Status 200 'Listar perfiles'
    $AdminProfileId = ((Get-Json) | Where-Object codigo -EQ 'ADMINISTRADOR' | Select-Object -First 1).id
    if (-not $AdminProfileId) { throw 'No existe perfil ADMINISTRADOR.' }
    Invoke-Api GET "/api/perfiles/$AdminProfileId/permisos" $script:AdminToken
    Assert-Status 200 'Consultar permisos del perfil'
    $PermissionIds = @((Get-Json) | ForEach-Object id)
    Invoke-Api PUT "/api/perfiles/$AdminProfileId/permisos" $script:AdminToken @{
        permisosIds = $PermissionIds
    }
    Assert-Status 200 'Actualizar permisos del perfil'
    Write-Pass 'perfiles: lectura y permisos'

    $UserId = [string](Get-Random -Minimum 10000000 -Maximum 99999999)
    Invoke-Api POST '/api/usuarios' $script:AdminToken @{
        identificacion = $UserId; nombres='Usuario Smoke'; apellidos='Temporal'
        correo="smoke-$UserId@example.test"; perfilId=$AdminProfileId
    }
    Assert-Status 201 'Crear usuario'
    if ((Get-Json).PSObject.Properties.Name -contains 'passwordHash') {
        throw 'La respuesta de usuario expone passwordHash.'
    }
    Invoke-Api GET "/api/usuarios/$UserId" $script:AdminToken
    Assert-Status 200 'Leer usuario'
    Invoke-Api PUT "/api/usuarios/$UserId" $script:AdminToken @{
        nombres='Usuario Smoke Actualizado'; apellidos='Temporal'
        correo="smoke-$UserId@example.test"; perfilId=$AdminProfileId; estado='INACTIVO'
    }
    Assert-Status 200 'Actualizar usuario'
    Invoke-Api PUT "/api/usuarios/$UserId" $script:AdminToken @{
        nombres='Usuario Smoke Actualizado'; apellidos='Temporal'
        correo="smoke-$UserId@example.test"; perfilId=$AdminProfileId; estado='ACTIVO'
    }
    Assert-Status 200 'Reactivar usuario'
    Write-Pass 'usuarios admin: crear, leer, actualizar; sin passwordHash'

    Invoke-Api GET '/api/usuarios'
    Assert-Status 401 'Ruta admin sin token'
    Invoke-Api GET '/api/usuarios' 'token-invalido'
    Assert-Status 401 'Ruta admin con token invalido'
    Write-Pass 'rutas admin: 401 sin token y con token invalido'

    Invoke-Api POST '/api/auth/login' '' @{
        identificacion=$AdminId; contrasena='Incorrecta-1'; recordarContrasena=$false
    }
    Assert-Status 401 'Credenciales incorrectas'
    $KnownMessage = (Get-Json).mensaje
    Invoke-Api POST '/api/auth/login' '' @{
        identificacion='8999999987'; contrasena='Incorrecta-1'; recordarContrasena=$false
    }
    Assert-Status 401 'Credenciales inexistentes'
    if ($KnownMessage -ne 'Credenciales inválidas' -or (Get-Json).mensaje -ne $KnownMessage) {
        throw 'El mensaje de credenciales no es generico.'
    }
    Write-Pass 'credenciales incorrectas: 401 y mensaje generico'

    1..6 | ForEach-Object {
        Invoke-Api POST '/api/auth/login' '' @{
            identificacion=$Personal.Id; contrasena='Wrong-1'; recordarContrasena=$false
        }
        Assert-Status 401 "Fallo de login $_"
    }
    Invoke-Api POST '/api/auth/login' '' @{
        identificacion=$Personal.Id; contrasena=$PersonalPassword; recordarContrasena=$false
    }
    Assert-Status 401 'Bloqueo tras seis fallos'
    Write-Pass 'bloqueo de login tras 6 fallos'

    Invoke-Api POST '/api/auth/logout' $script:AdminToken
    Assert-Status 204 'Logout'
    Invoke-Api GET '/api/usuarios' $script:AdminToken
    Assert-Status 401 'Token revocado'
    Write-Pass 'logout y revocacion del token (401)'
} catch {
    Write-Error "FAIL: $_"
    exit 1
} finally {
    Restore-Antecedents
    $Client.Dispose()
}

if ($Failures.Count -gt 0) {
    Write-Error "Resultado: $($Failures.Count) comprobacion(es) fallaron: $($Failures -join '; ')"
    exit 1
}
Write-Output 'RESULTADO: todas las comprobaciones del smoke test finalizaron correctamente.'
