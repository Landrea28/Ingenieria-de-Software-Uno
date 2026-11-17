# OWASP ZAP baseline (opcional)

No forma parte del build. Inicia el backend en `localhost:8080` con el perfil
demo. En Linux, ejecuta:

```sh
docker run --rm -t --network host ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py -t http://127.0.0.1:8080 -m 5
```

`--network host` solo funciona como se espera en Linux. En Windows, omite esa
opción y apunta al host de Docker:

```powershell
docker run --rm -t ghcr.io/zaproxy/zaproxy:stable zap-baseline.py `
  -t http://host.docker.internal:8080 -m 5
```

El baseline realiza un escaneo pasivo. No configura autenticación ni intenta
modificar datos. Las rutas administrativas deben continuar protegidas;
cualquier hallazgo se valida manualmente antes de considerarlo vulnerabilidad.

## Ejecución registrada

El 2026-11-16 se ejecutó el comando Linux anterior contra el backend local con
el perfil `demo`. Resultado: 67 reglas PASS, cero FAIL y un WARN de
`Non-Storable Content` en `/`, `/robots.txt` y `/sitemap.xml`; ZAP terminó con
código 2 por el aviso. La validación manual confirmó que las tres respuestas
son HTTP 401 y envían `Cache-Control: no-cache, no-store, max-age=0,
must-revalidate`, por lo que no se observó contenido almacenable. La salida
completa está en
[`../evidencias/zap-baseline-salida.txt`](../evidencias/zap-baseline-salida.txt).
El spider no pudo explorar la raíz sin autenticación (401); el resultado cubre
solo el alcance anónimo.
