
export type CodigoFuerza = 'POLICIA' | 'EJERCITO' | 'ARMADA' | 'FUERZA_AEREA';

export interface Rango {
  id: number;
  nombre: string;
}

export interface RangoResponse {
  id?: number;
  nombre?: string;
}

export interface Fuerza {
  codigo: CodigoFuerza;
  nombre: string;
  rangos: Rango[];
}

export interface FuerzaResponse {
  codigo?: string;
  nombre?: string;
  rangos?: RangoResponse[];
}

export interface RegistroPersonalRequest {
  identificacion: string;
  nombres: string;
  apellidos: string;
  fuerzaCodigo: CodigoFuerza;
  rangoId: number;
  idFuerza: string;
  correo: string;
}

export interface RegistroPersonalResponse {
  mensaje?: string;
}

export interface LoginRequest {
  identificacion: string;
  contrasena: string;
  recordarContrasena: boolean;
}

export interface LoginResponse {
  token?: string;
  tipo?: string;
  expiraEnSegundos?: number;
}

export type EstadoAntecedente = 'REQUERIDO' | 'NO_REQUERIDO';

export interface AntecedenteResponse {
  identificacion?: string;
  estado?: EstadoAntecedente;
  fuente?: string;
  fechaActualizacion?: string;
}

export type CodigoError =
  | 'VALIDACION'
  | 'NO_ENCONTRADO'
  | 'DUPLICADO'
  | 'SIN_INFORMACION'
  | 'SIN_RESULTADOS'
  | 'INTEGRACION_PENDIENTE'
  | 'NO_AUTENTICADO'
  | 'ACCESO_DENEGADO'
  | 'ERROR_INTERNO';

export interface ErrorResponse {
  codigo: CodigoError;
  mensaje: string;
  detalles: string[];
}
