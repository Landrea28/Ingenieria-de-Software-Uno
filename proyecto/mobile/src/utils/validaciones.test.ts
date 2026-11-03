import assert from 'node:assert/strict';
import test from 'node:test';
import { validarCorreo, validarIdentificacion, validarRegistro, type DatosRegistro } from './validaciones.ts';

const valido: DatosRegistro = {
  identificacion: '8000000001',
  nombres: 'Usuario',
  apellidos: 'Prueba Uno',
  fuerzaCodigo: 'POLICIA',
  rangoId: 1,
  idFuerza: 'DEMO-001',
  correo: 'demo@example.test',
};

test('un registro correcto no produce errores', () => {
  assert.deepEqual(validarRegistro(valido), {});
});

test('la identificación debe ser numérica de 6 a 10 dígitos', () => {
  assert.equal(validarIdentificacion('12345'), 'Debe tener solo números, entre 6 y 10 dígitos');
  assert.equal(validarIdentificacion('12345678901'), 'Debe tener solo números, entre 6 y 10 dígitos');
  assert.equal(validarIdentificacion('12a45678'), 'Debe tener solo números, entre 6 y 10 dígitos');
  assert.equal(validarIdentificacion(''), 'La identificación es obligatoria');
  assert.equal(validarIdentificacion('123456'), undefined);
  assert.equal(validarIdentificacion('1234567890'), undefined);
});

test('el correo exige formato válido', () => {
  assert.equal(validarCorreo('sin-arroba'), 'El formato del correo no es válido');
  assert.equal(validarCorreo('a@b'), 'El formato del correo no es válido');
  assert.equal(validarCorreo(''), 'El correo es obligatorio');
  assert.equal(validarCorreo('a@b.co'), undefined);
});

test('los campos obligatorios vacíos se reportan todos', () => {
  const errores = validarRegistro({
    identificacion: '',
    nombres: '',
    apellidos: '',
    fuerzaCodigo: null,
    rangoId: null,
    idFuerza: '',
    correo: '',
  });
  assert.deepEqual(Object.keys(errores).sort(), [
    'apellidos',
    'correo',
    'fuerzaCodigo',
    'idFuerza',
    'identificacion',
    'nombres',
    'rangoId',
  ]);
});

test('los nombres no admiten números ni símbolos pero sí tildes y ñ', () => {
  assert.ok(validarRegistro({ ...valido, nombres: 'Juan3' }).nombres);
  assert.ok(validarRegistro({ ...valido, apellidos: 'Pérez-Núñez' }).apellidos);
  assert.deepEqual(validarRegistro({ ...valido, nombres: 'José Ángel', apellidos: 'Peña Núñez' }), {});
});
