/**
 * Prueba de integración del flujo móvil contra el BACKEND REAL (no el servidor falso).
 *
 * Usa el mismo cliente HTTP y los mismos servicios que usan las pantallas de la app
 * (src/api), así que valida el contrato tal como lo consume el móvil:
 * catálogo → registro → correo con contraseña → login → antecedentes → logout.
 *
 * Requisitos (ver README, sección «Prueba de integración contra el backend real»):
 *   - Backend con perfiles demo,dev sobre una base limpia, y Mailpit (docker compose).
 *   - EXPO_PUBLIC_API_URL apuntando al backend (por defecto http://127.0.0.1:8080).
 *   - MAILPIT_URL (por defecto http://127.0.0.1:8025).
 *
 * Todos los datos son ficticios y SIMULADOS (dataset demo del backend).
 */
import assert from 'node:assert/strict';
import test from 'node:test';

process.env.EXPO_PUBLIC_API_MODO = 'real';
process.env.EXPO_PUBLIC_API_URL ??= 'http://127.0.0.1:8080';
const MAILPIT_URL = (process.env.MAILPIT_URL ?? 'http://127.0.0.1:8025').replace(/\/+$/, '');

// Se cargan después de fijar el entorno: src/config/env lee las variables al importarse.
// eslint-disable-next-line @typescript-eslint/no-require-imports
const cliente = require('../src/api/cliente') as typeof import('../src/api/cliente');
// eslint-disable-next-line @typescript-eslint/no-require-imports
const servicios = require('../src/api/servicios') as typeof import('../src/api/servicios');
// eslint-disable-next-line @typescript-eslint/no-require-imports
const { mensajeDeError } = require('../src/api/mensajes') as typeof import('../src/api/mensajes');

/** Identidad SIMULADA del dataset demo de Defensa (V101__defensa_personal_ficticio.sql). */
const PERSONA = {
  identificacion: process.env.PRUEBA_IDENTIFICACION ?? '8000000005',
  nombres: 'Ana',
  apellidos: 'Prueba5',
  fuerzaCodigo: 'POLICIA' as const,
  rango: 'Subintendente',
  idFuerza: 'DEMO-005',
  correo: 'ana.prueba5@example.test',
};

const FORMATO_CONTRASENA = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{6}$/;

async function esperarError(promesa: Promise<unknown>): Promise<InstanceType<typeof cliente.ApiError>> {
  try {
    await promesa;
  } catch (error) {
    assert.ok(error instanceof cliente.ApiError, 'se esperaba un ApiError');
    return error;
  }
  assert.fail('se esperaba que la solicitud fallara');
}

/** Lee de Mailpit la contraseña enviada al correo de la persona registrada. */
async function contrasenaDelCorreo(correo: string): Promise<string> {
  for (let intento = 0; intento < 20; intento++) {
    const lista = (await (await fetch(`${MAILPIT_URL}/api/v1/search?query=${encodeURIComponent(`to:${correo}`)}`)).json()) as {
      messages: { ID: string }[];
    };
    if (lista.messages.length > 0) {
      const mensaje = (await (await fetch(`${MAILPIT_URL}/api/v1/message/${lista.messages[0].ID}`)).json()) as {
        Text: string;
      };
      const candidatas = mensaje.Text.split(/[^A-Za-z0-9]+/).filter((p) => FORMATO_CONTRASENA.test(p));
      assert.equal(candidatas.length, 1, 'el correo debe traer exactamente una contraseña de 6 caracteres');
      return candidatas[0];
    }
    await new Promise((r) => setTimeout(r, 500));
  }
  assert.fail(`no llegó ningún correo a ${correo} en Mailpit`);
}

let rangoId = 0;
let contrasena = '';

test('catálogo: cuatro fuerzas con rangos dependientes (público, sin token)', async () => {
  const fuerzas = await servicios.obtenerFuerzasRangos();
  assert.deepEqual(fuerzas.map((f) => f.codigo).sort(), ['ARMADA', 'EJERCITO', 'FUERZA_AEREA', 'POLICIA']);
  for (const fuerza of fuerzas) assert.ok(fuerza.rangos.length > 0, `${fuerza.codigo} sin rangos`);
  const rango = fuerzas.find((f) => f.codigo === PERSONA.fuerzaCodigo)?.rangos.find((r) => r.nombre === PERSONA.rango);
  assert.ok(rango, 'el rango de la persona de prueba debe existir en el catálogo');
  rangoId = rango.id;
});

test('registro rechazado: datos que Defensa (SIMULADA) no valida → 400 con mensaje genérico', async () => {
  const error = await esperarError(
    servicios.registrarPersonal({
      identificacion: '8000000099',
      nombres: 'Nadie',
      apellidos: 'Inexistente',
      fuerzaCodigo: 'POLICIA',
      rangoId,
      idFuerza: 'DEMO-099',
      correo: 'nadie@example.test',
    }),
  );
  assert.equal(error.estado, 400);
  assert.equal(error.codigo, 'VALIDACION');
});

test('registro rechazado: campos con formato inválido → 400 con detalles por campo', async () => {
  const error = await esperarError(
    servicios.registrarPersonal({
      identificacion: 'abc',
      nombres: '',
      apellidos: '',
      fuerzaCodigo: 'POLICIA',
      rangoId,
      idFuerza: '',
      correo: 'no-es-correo',
    }),
  );
  assert.equal(error.estado, 400);
  assert.ok(error.detalles.length > 0, 'el backend debe listar los campos inválidos');
});

test('registro aceptado: la respuesta no trae la contraseña y esta llega por correo con 6 caracteres', async () => {
  const respuesta = await servicios.registrarPersonal({
    identificacion: PERSONA.identificacion,
    nombres: PERSONA.nombres,
    apellidos: PERSONA.apellidos,
    fuerzaCodigo: PERSONA.fuerzaCodigo,
    rangoId,
    idFuerza: PERSONA.idFuerza,
    correo: PERSONA.correo,
  });
  assert.deepEqual(Object.keys(respuesta), ['mensaje'], 'la respuesta solo debe traer un mensaje');
  contrasena = await contrasenaDelCorreo(PERSONA.correo);
  assert.match(contrasena, FORMATO_CONTRASENA);
  assert.ok(!JSON.stringify(respuesta).includes(contrasena), 'la API nunca devuelve la contraseña');
});

test('registro duplicado: la misma identificación se rechaza sin crear otra cuenta', async () => {
  const error = await esperarError(
    servicios.registrarPersonal({
      identificacion: PERSONA.identificacion,
      nombres: PERSONA.nombres,
      apellidos: PERSONA.apellidos,
      fuerzaCodigo: PERSONA.fuerzaCodigo,
      rangoId,
      idFuerza: PERSONA.idFuerza,
      correo: PERSONA.correo,
    }),
  );
  assert.ok(error.estado === 400 || error.estado === 409, `estado inesperado ${error.estado}`);
});

test('login: contraseña incorrecta → 401 sin disparar «sesión vencida»', async () => {
  let expiraciones = 0;
  cliente.registrarAlExpirarSesion(() => expiraciones++);
  const error = await esperarError(
    servicios.iniciarSesionApi({ identificacion: PERSONA.identificacion, contrasena: 'Zz999z', recordarContrasena: false }),
  );
  assert.equal(error.estado, 401);
  assert.equal(expiraciones, 0);
});

test('antecedentes sin sesión → 401 (la ruta está protegida en el servidor)', async () => {
  cliente.fijarToken(null);
  const error = await esperarError(servicios.consultarAntecedente('9000000003'));
  assert.equal(error.estado, 401);
});

test('login válido: token Bearer; «Recordar contraseña» alarga la vigencia', async () => {
  const corta = await servicios.iniciarSesionApi({
    identificacion: PERSONA.identificacion,
    contrasena,
    recordarContrasena: false,
  });
  const larga = await servicios.iniciarSesionApi({
    identificacion: PERSONA.identificacion,
    contrasena,
    recordarContrasena: true,
  });
  for (const r of [corta, larga]) {
    assert.equal(typeof r.token, 'string');
    assert.ok(typeof r.expiraEnSegundos === 'number' && r.expiraEnSegundos > 0);
    assert.ok(!JSON.stringify(r).includes(contrasena), 'el login no devuelve la contraseña');
  }
  assert.ok(larga.expiraEnSegundos! > corta.expiraEnSegundos!, 'recordar debe emitir un token de mayor duración');
  cliente.fijarToken(larga.token!);
});

test('antecedentes: el estado lo decide el backend (REQUERIDO / NO_REQUERIDO)', async () => {
  const requerido = await servicios.consultarAntecedente('9000000003');
  assert.equal(requerido.estado, 'REQUERIDO');
  assert.equal(requerido.identificacion, '9000000003');
  const noRequerido = await servicios.consultarAntecedente('9000000001');
  assert.equal(noRequerido.estado, 'NO_REQUERIDO');
  assert.equal(noRequerido.fuente, 'SIMULADA');
});

test('antecedentes: ciudadano sin estado o inexistente → 404 SIN_INFORMACION, nunca NO_REQUERIDO', async () => {
  for (const identificacion of ['9000000020', '9111111111']) {
    const error = await esperarError(servicios.consultarAntecedente(identificacion));
    assert.equal(error.estado, 404);
    assert.equal(error.codigo, 'SIN_INFORMACION');
  }
});

test('logout: el token queda revocado y la siguiente consulta dispara «sesión vencida»', async () => {
  let expiraciones = 0;
  cliente.registrarAlExpirarSesion(() => expiraciones++);
  await servicios.cerrarSesionApi();
  const error = await esperarError(servicios.consultarAntecedente('9000000003'));
  assert.equal(error.estado, 401);
  assert.equal(expiraciones, 1, 'un 401 con token enviado debe devolver al login');
  cliente.fijarToken(null);
});

test('sin conexión: el cliente lo reporta como error de red con un mensaje claro', async () => {
  // Módulo de cliente aislado apuntando a un puerto cerrado.
  const anterior = process.env.EXPO_PUBLIC_API_URL;
  process.env.EXPO_PUBLIC_API_URL = 'http://127.0.0.1:9';
  for (const ruta of ['../src/api/cliente', '../src/config/env']) delete require.cache[require.resolve(ruta)];
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const aislado = require('../src/api/cliente') as typeof import('../src/api/cliente');
  process.env.EXPO_PUBLIC_API_URL = anterior;
  try {
    await aislado.solicitar('/api/health', { autenticado: false });
    assert.fail('se esperaba un error de red');
  } catch (error) {
    assert.ok(error instanceof aislado.ApiError);
    assert.equal(error.tipo, 'red');
    assert.match(error.message, /conexión/);
  }
  assert.equal(typeof mensajeDeError, 'function');
});
