import { ApiError, solicitar } from './cliente';
import type {
  AntecedenteResponse,
  Fuerza,
  FuerzaResponse,
  LoginRequest,
  LoginResponse,
  RegistroPersonalRequest,
  RegistroPersonalResponse,
} from '../types/api';

const CODIGOS_FUERZA = new Set(['POLICIA', 'EJERCITO', 'ARMADA', 'FUERZA_AEREA']);

function respuestaInvalida(): never {
  throw new ApiError('respuesta', 200, null, 'El catálogo de fuerzas y rangos tiene un formato inesperado');
}

/**
 * Operaciones del contrato HTTP agrupadas por funcionalidad.
 * Esta capa delega el transporte al cliente y no contiene lógica de negocio.
 */

/** Obtiene el catálogo público de fuerzas y rangos disponibles. */
export async function obtenerFuerzasRangos(): Promise<Fuerza[]> {
  const respuesta = await solicitar<unknown>('/api/catalogos/fuerzas-rangos', { autenticado: false });
  if (!Array.isArray(respuesta)) respuestaInvalida();

  return (respuesta as FuerzaResponse[]).map((fuerza) => {
    if (
      !fuerza ||
      typeof fuerza.codigo !== 'string' ||
      !CODIGOS_FUERZA.has(fuerza.codigo) ||
      typeof fuerza.nombre !== 'string' ||
      !Array.isArray(fuerza.rangos)
    ) {
      respuestaInvalida();
    }

    const rangos = fuerza.rangos.map((rango) => {
      if (
        !rango ||
        typeof rango.id !== 'number' ||
        !Number.isSafeInteger(rango.id) ||
        typeof rango.nombre !== 'string'
      ) {
        respuestaInvalida();
      }
      return { id: rango.id, nombre: rango.nombre };
    });

    return {
      codigo: fuerza.codigo as Fuerza['codigo'],
      nombre: fuerza.nombre,
      rangos,
    };
  });
}

/** Registra una cuenta de personal con los datos proporcionados. */
export const registrarPersonal = (datos: RegistroPersonalRequest) =>
  solicitar<RegistroPersonalResponse>('/api/personal/registro', {
    metodo: 'POST',
    cuerpo: datos,
    autenticado: false,
  });

/** Autentica al usuario y devuelve los datos de sesión definidos por la API. */
export const iniciarSesionApi = (datos: LoginRequest) =>
  solicitar<LoginResponse>('/api/auth/login', {
    metodo: 'POST',
    cuerpo: datos,
    autenticado: false,
  });

/** Invalida en el servidor la sesión autenticada actual. */
export const cerrarSesionApi = () => solicitar<void>('/api/auth/logout', { metodo: 'POST' });

/** Consulta los antecedentes asociados a una identificación ciudadana. */
export const consultarAntecedente = (identificacion: string) =>
  solicitar<AntecedenteResponse>(`/api/antecedentes/${encodeURIComponent(identificacion)}`);
