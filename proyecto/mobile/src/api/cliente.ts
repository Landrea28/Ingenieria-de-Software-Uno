import { env } from '../config/env';
import type { ErrorResponse } from '../types/api';

export type TipoErrorApi = 'http' | 'red' | 'timeout' | 'respuesta';

/** Representa de forma uniforme los errores producidos al comunicarse con la API. */
export class ApiError extends Error {
  constructor(
    public readonly tipo: TipoErrorApi,
    public readonly estado: number | null,
    public readonly codigo: string | null,
    mensaje: string,
    public readonly detalles: string[] = [],
  ) {
    super(mensaje);
    this.name = 'ApiError';
  }
}

/** Token de acceso vigente, almacenado únicamente en memoria. */
let tokenActual: string | null = null;
let alExpirarSesion: (() => void) | null = null;

/** Actualiza el token que se enviará en las solicitudes autenticadas. */
export function fijarToken(token: string | null): void {
  tokenActual = token;
}

/** Registra el manejador que se ejecutará al detectar una sesión expirada. */
export function registrarAlExpirarSesion(manejador: (() => void) | null): void {
  alExpirarSesion = manejador;
}

interface Opciones {
  metodo?: 'GET' | 'POST' | 'PUT';
  cuerpo?: unknown;
  /** Si es true y hay token, se envía "Authorization: Bearer ...". */
  autenticado?: boolean;
}

export async function solicitar<T>(ruta: string, opciones: Opciones = {}): Promise<T> {
  const { metodo = 'GET', cuerpo, autenticado = true } = opciones;
  const encabezados: Record<string, string> = { Accept: 'application/json' };
  const enviaToken = autenticado && tokenActual !== null;
  if (enviaToken) encabezados.Authorization = `Bearer ${tokenActual}`;
  if (cuerpo !== undefined) encabezados['Content-Type'] = 'application/json';

  const controlador = new AbortController();
  const temporizador = setTimeout(() => controlador.abort(), env.API_TIMEOUT_MS);

  let respuesta: Response;
  try {
    respuesta = await fetch(`${env.API_URL}${ruta}`, {
      method: metodo,
      headers: encabezados,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
      signal: controlador.signal,
    });
  } catch (error) {
    clearTimeout(temporizador);
    if (controlador.signal.aborted) {
      throw new ApiError('timeout', null, null, 'El servidor tardó demasiado en responder');
    }
    throw new ApiError('red', null, null, 'No hay conexión con el servidor');
  }

  try {
    if (respuesta.ok) {
      if (respuesta.status === 204) return undefined as T;
      try {
        return (await respuesta.json()) as T;
      } catch {
        if (controlador.signal.aborted) {
          throw new ApiError('timeout', null, null, 'El servidor tardó demasiado en responder');
        }
        throw new ApiError('respuesta', respuesta.status, null, 'Respuesta inesperada del servidor');
      }
    }

    // Error HTTP: el backend responde con el formato ErrorResponse; si no, se degrada con gracia.
    let cuerpoError: Partial<ErrorResponse> = {};
    try {
      cuerpoError = (await respuesta.json()) as Partial<ErrorResponse>;
    } catch {
      if (controlador.signal.aborted) {
        throw new ApiError('timeout', null, null, 'El servidor tardó demasiado en responder');
      }
      // cuerpo vacío o no JSON
    }

    // 401 con token enviado = sesión vencida o revocada → volver al login.
    // (Un 401 sin token, p. ej. credenciales malas en el login, NO dispara esto.)
    if (respuesta.status === 401 && enviaToken) alExpirarSesion?.();

    throw new ApiError(
      'http',
      respuesta.status,
      cuerpoError.codigo ?? null,
      cuerpoError.mensaje ?? `Error ${respuesta.status}`,
      cuerpoError.detalles ?? [],
    );
  } finally {
    clearTimeout(temporizador);
  }
}
