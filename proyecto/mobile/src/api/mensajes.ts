import * as cliente from './cliente';

/**
 * Traduce errores de transporte y respuestas HTTP a mensajes comprensibles
 * para su presentación en la interfaz de usuario.
 */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof cliente.ApiError)) return 'Ocurrió un error inesperado. Intenta de nuevo.';
  switch (error.tipo) {
    case 'red':
      return 'No hay conexión con el servidor. Revisa tu internet y la dirección configurada.';
    case 'timeout':
      return 'El servidor tardó demasiado en responder. Intenta de nuevo.';
    case 'respuesta':
      return 'El servidor respondió algo inesperado. Intenta de nuevo.';
    case 'http':
      if (error.estado === 503 || error.codigo === 'INTEGRACION_PENDIENTE') {
        return 'Esta función aún no está disponible en el servidor (integración pendiente).';
      }
      if (error.estado !== null && error.estado >= 500) {
        return 'El servidor tuvo un problema. Intenta más tarde.';
      }
      return error.message;
  }
}
