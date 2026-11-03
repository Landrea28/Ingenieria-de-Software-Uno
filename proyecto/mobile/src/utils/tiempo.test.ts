import assert from 'node:assert/strict';
import test from 'node:test';
import { calcularExpiracion, estaVigente, leerExpiracionJwt } from './tiempo.ts';

test('calcula el instante de expiración a partir de los segundos', () => {
  assert.equal(calcularExpiracion(1800, 1_000_000), 1_000_000 + 1_800_000);
});

test('un token con tiempo restante está vigente', () => {
  assert.equal(estaVigente(1_100_000, 1_000_000), true);
});

test('un token vencido o por vencer (dentro del margen) no está vigente', () => {
  assert.equal(estaVigente(999_000, 1_000_000), false);
  assert.equal(estaVigente(1_003_000, 1_000_000), false); // vence en 3 s < margen de 5 s
});

test('una fecha de expiración inválida se trata como vencida', () => {
  assert.equal(estaVigente(Number.NaN, 1_000_000), false);
});

test('lee el vencimiento estándar exp de un JWT', () => {
  assert.equal(leerExpiracionJwt('header.eyJleHAiOjE4MDB9.signature'), 1_800_000);
  assert.equal(leerExpiracionJwt('token-invalido'), null);
});
