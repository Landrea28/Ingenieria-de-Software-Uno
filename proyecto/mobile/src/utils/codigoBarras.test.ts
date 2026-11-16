import assert from 'node:assert/strict';
import test from 'node:test';
import { extraerIdentificacion } from './codigoBarras.ts';

test('acepta una identificación numérica simple de 6 a 10 dígitos', () => {
  assert.equal(extraerIdentificacion('9000000001'), '9000000001');
  assert.equal(extraerIdentificacion('800000'), '800000');
});

test('quita espacios y separadores comunes antes de validar', () => {
  assert.equal(extraerIdentificacion(' 9000000001 '), '9000000001');
  assert.equal(extraerIdentificacion('900-000-0001'), '9000000001');
  assert.equal(extraerIdentificacion('900.000.0001'), '9000000001');
});

test('rechaza datos que no quedan en 6 a 10 dígitos', () => {
  assert.equal(extraerIdentificacion('12345'), null);
  assert.equal(extraerIdentificacion('12345678901'), null);
  assert.equal(extraerIdentificacion(''), null);
});

test('rechaza datos con letras u otro contenido (p. ej. una URL de un QR)', () => {
  assert.equal(extraerIdentificacion('https://example.com/9000000001'), null);
  assert.equal(extraerIdentificacion('ABC9000000001'), null);
});
