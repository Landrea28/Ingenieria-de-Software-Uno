import assert from 'node:assert/strict';
import test from 'node:test';
import { describirError, traducirDetalle, type InfoError } from './errores.ts';

const http = (estado: number, codigo: string | null = null, detalles: string[] = []): InfoError => ({
  tipo: 'http',
  estado,
  codigo,
  detalles,
});

test('login: 401 explica de dónde sale la contraseña y el bloqueo temporal', () => {
  const e = describirError(http(401, 'NO_AUTENTICADO'), 'login');
  assert.equal(e.mensaje, 'Identificación o contraseña incorrectas.');
  assert.match(e.sugerencia ?? '', /correo/);
  assert.match(e.sugerencia ?? '', /bloquea/);
});

test('registro: 409 DUPLICADO manda a iniciar sesión en vez de pedir registrarse otra vez', () => {
  const e = describirError(http(409, 'DUPLICADO'), 'registro');
  assert.match(e.mensaje, /ya tienen una cuenta/);
  assert.equal(e.accion, 'ir-a-login');
});

test('registro: 409 LIMITE_REGISTRO no se confunde con un duplicado', () => {
  const e = describirError(http(409, 'LIMITE_REGISTRO'), 'registro');
  assert.match(e.mensaje, /demasiados intentos/);
  assert.equal(e.accion, undefined);
});

test('registro: 400 sin detalles indica que Defensa no validó los datos', () => {
  const e = describirError(http(400, 'VALIDACION'), 'registro');
  assert.match(e.mensaje, /Departamento de Defensa/);
  assert.equal(e.detalles, undefined);
});

test('registro: 400 con detalles los traduce a lenguaje claro', () => {
  const e = describirError(
    http(400, 'VALIDACION', ['correo: no debe estar vacío', 'identificacion: debe coincidir con "[0-9]{6,10}"']),
    'registro',
  );
  assert.deepEqual(e.detalles, [
    'Correo electrónico: es obligatorio',
    'Identificación: debe tener solo números, entre 6 y 10 dígitos',
  ]);
});

test('traducirDetalle conserva el texto si no lo reconoce y deja intactos los textos sin campo', () => {
  assert.equal(traducirDetalle('rangoId: no debe ser nulo'), 'Rango: es obligatorio');
  assert.equal(traducirDetalle('campoNuevo: algo raro'), 'campoNuevo: algo raro');
  assert.equal(traducirDetalle('Datos invalidos'), 'Datos invalidos');
});

test('errores de red, tiempo de espera y respuesta tienen mensajes distintos con una sugerencia', () => {
  const red = describirError({ tipo: 'red', estado: null, codigo: null, detalles: [] });
  const espera = describirError({ tipo: 'timeout', estado: null, codigo: null, detalles: [] });
  const rara = describirError({ tipo: 'respuesta', estado: 200, codigo: null, detalles: [] });
  assert.equal(new Set([red.mensaje, espera.mensaje, rara.mensaje]).size, 3);
  for (const e of [red, espera, rara]) assert.ok(e.sugerencia);
});

test('el catálogo nombra lo que no se pudo cargar', () => {
  const e = describirError({ tipo: 'red', estado: null, codigo: null, detalles: [] }, 'catalogo');
  assert.match(e.mensaje, /fuerzas y rangos/);
});

test('503, 5xx, 403 y 404 generales', () => {
  assert.match(describirError(http(503, 'INTEGRACION_PENDIENTE')).mensaje, /no está disponible/);
  assert.match(describirError(http(500, 'ERROR_INTERNO')).mensaje, /problema/);
  assert.match(describirError(http(403, 'ACCESO_DENEGADO')).mensaje, /permiso/);
  assert.match(describirError(http(404, 'NO_ENCONTRADO')).mensaje, /No se encontró/);
});

test('un 401 fuera del login significa sesión vencida y ofrece volver a iniciar sesión', () => {
  const e = describirError(http(401, 'NO_AUTENTICADO'), 'consulta');
  assert.equal(e.mensaje, 'Tu sesión venció.');
  assert.equal(e.accion, 'ir-a-login');
});

test('un error desconocido no deja la pantalla sin mensaje', () => {
  assert.ok(describirError(null).mensaje.length > 0);
});
