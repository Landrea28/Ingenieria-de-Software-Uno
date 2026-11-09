# Pruebas de seguridad con Postman y Newman

La coleccion usa `baseUrl`, identificaciones, contrasenas y tokens como variables. El entorno versionado no contiene credenciales reales; completa localmente las variables `identificacionPersonal`, `contrasenaPersonal`, `identificacionAdministrador` y `contrasenaAdministrador`.

Arranca PostgreSQL y el backend con perfil `demo` para contar con los datos ficticios. El perfil demo y el envio de correo siguen siendo SIMULADOS.

Instala Newman y ejecuta desde `proyecto/backend`:

```sh
npm install --global newman
newman run docs/pruebas/postman/seguridad.postman_collection.json \
  --environment docs/pruebas/postman/seguridad.postman_environment.json
```

Tambien puedes pasar datos solo en la invocacion, sin guardarlos en el repositorio:

```sh
newman run docs/pruebas/postman/seguridad.postman_collection.json \
  --environment docs/pruebas/postman/seguridad.postman_environment.json \
  --env-var identificacionPersonal=IDENTIFICACION_LOCAL \
  --env-var contrasenaPersonal=CONTRASENA_LOCAL \
  --env-var identificacionAdministrador=IDENTIFICACION_ADMIN_LOCAL \
  --env-var contrasenaAdministrador=CONTRASENA_ADMIN_LOCAL
```

Los tokens se guardan en memoria en las variables `tokenPersonal` y `tokenAdministrador` al ejecutar los requests de login. No exportes un entorno con credenciales o tokens.
