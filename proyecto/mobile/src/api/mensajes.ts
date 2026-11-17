import { env } from '../config/env';
import { describirError, type ContextoError, type ErrorUsuario } from '../utils/errores';
import * as cliente from './cliente';

/** Pista extra solo para quien desarrolla: en un teléfono físico casi todos los fallos de red vienen de aquí. */
function pistaDeDesarrollo(): string {
  return (
    ` (Dirección usada: ${env.API_URL}. En un teléfono usa la IP del computador en EXPO_PUBLIC_API_URL, ` +
    'la misma red wifi y acepta el permiso de red local de Expo Go.)'
  );
}

// React Native define __DEV__ como global; con globalThis también funciona fuera de la app (pruebas).
const esDesarrollo = (globalThis as { __DEV__?: boolean }).__DEV__ === true;

/**
 * Traduce un error de la API a un mensaje para la persona: qué pasó y qué puede hacer.
 * La lógica de textos vive en utils/errores.ts; aquí solo se adapta `ApiError`.
 */
export function describirErrorApi(error: unknown, contexto: ContextoError = 'general'): ErrorUsuario {
  if (!(error instanceof cliente.ApiError)) return describirError(null, contexto);
  const descrito = describirError(
    { tipo: error.tipo, estado: error.estado, codigo: error.codigo, detalles: error.detalles },
    contexto,
  );
  if (esDesarrollo && (error.tipo === 'red' || error.tipo === 'timeout')) {
    return { ...descrito, sugerencia: `${descrito.sugerencia ?? ''}${pistaDeDesarrollo()}`.trim() };
  }
  return descrito;
}
