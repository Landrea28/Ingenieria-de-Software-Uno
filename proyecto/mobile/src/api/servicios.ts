import { solicitar } from './cliente';
import type {
  AntecedenteResponse,
  Fuerza,
  LoginRequest,
  LoginResponse,
  RegistroPersonalRequest,
  RegistroPersonalResponse,
} from '../types/api';

/**
 * Operaciones del contrato HTTP agrupadas por funcionalidad.
 * Esta capa delega el transporte al cliente y no contiene lógica de negocio.
 */

/** Obtiene el catálogo público de fuerzas y rangos disponibles. */
export const obtenerFuerzasRangos = () =>
  solicitar<Fuerza[]>('/api/catalogos/fuerzas-rangos', { autenticado: false });

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
