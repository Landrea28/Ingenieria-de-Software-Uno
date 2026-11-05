# OWASP ZAP baseline (opcional)

No forma parte del build. Inicia el backend en `localhost:8080` con el perfil demo y usa Docker en Linux:

```sh
docker run --rm -t --network host ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py -t http://127.0.0.1:8080 -m 5
```

El baseline realiza un escaneo pasivo. No configura autenticacion ni intenta modificar datos. Las rutas administrativas deben continuar protegidas; cualquier hallazgo se valida manualmente antes de considerarlo vulnerabilidad.
