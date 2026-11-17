// Traducción de los errores de la API a mensajes que una persona entiende y puede
// resolver. Es lógica pura (sin React ni red) para poder probarla con `npm test`.
// El adaptador que convierte un `ApiError` a `InfoError` está en src/api/mensajes.ts.

/** Dónde ocurrió el error: el mismo código HTTP significa cosas distintas en cada pantalla. */
export type ContextoError = 'general' | 'login' | 'registro' | 'consulta' | 'catalogo';

/** Acción que la pantalla puede ofrecer junto al mensaje. */
export type AccionError = 'ir-a-login';

/** Datos del error tal como los entrega el cliente HTTP. */
export interface InfoError {
  tipo: 'http' | 'red' | 'timeout' | 'respuesta';
  estado: number | null;
  codigo: string | null;
  detalles: string[];
}

/** Lo que se muestra a la persona: qué pasó, qué datos fallaron y qué puede hacer. */
export interface ErrorUsuario {
  mensaje: string;
  /** Qué hacer para resolverlo. */
  sugerencia?: string;
  /** Campos con problemas, ya en lenguaje claro. */
  detalles?: string[];
  accion?: AccionError;
}

const ETIQUETAS_CAMPO: Record<string, string> = {
  identificacion: 'Identificación',
  nombres: 'Nombres',
  apellidos: 'Apellidos',
  fuerzaCodigo: 'Fuerza pública',
  rangoId: 'Rango',
  idFuerza: 'ID de fuerza',
  correo: 'Correo electrónico',
  contrasena: 'Contraseña',
};

/** Convierte «campo: texto técnico» del backend en «Campo: texto claro». */
export function traducirDetalle(detalle: string): string {
  const separador = detalle.indexOf(':');
  if (separador < 0) return detalle;
  const campo = detalle.slice(0, separador).trim();
  const texto = detalle.slice(separador + 1).trim();
  const etiqueta = ETIQUETAS_CAMPO[campo] ?? campo;

  let claro = texto;
  if (/no debe estar vac|no debe ser nulo|must not be (blank|null|empty)/i.test(texto)) {
    claro = 'es obligatorio';
  } else if (/debe coincidir con/i.test(texto)) {
    claro = campo === 'identificacion' ? 'debe tener solo números, entre 6 y 10 dígitos' : 'tiene un formato inválido';
  } else if (/correo|e-?mail/i.test(texto)) {
    claro = 'no es un correo válido';
  } else if (/tamaño|size must be/i.test(texto)) {
    claro = 'tiene una longitud no permitida';
  }
  return `${etiqueta}: ${claro}`;
}

const REINTENTAR_MAS_TARDE = 'Intenta de nuevo en unos minutos.';

function describirRegistro(info: InfoError): ErrorUsuario | null {
  if (info.tipo !== 'http') return null;
  if (info.estado === 409 && info.codigo === 'LIMITE_REGISTRO') {
    return {
      mensaje: 'Hiciste demasiados intentos de registro.',
      sugerencia: 'Espera unos 15 minutos y vuelve a intentarlo.',
    };
  }
  if (info.estado === 409) {
    return {
      mensaje: 'Esta identificación o este correo ya tienen una cuenta.',
      sugerencia: 'No necesitas registrarte otra vez: inicia sesión con la contraseña de 6 caracteres que llegó a tu correo.',
      accion: 'ir-a-login',
    };
  }
  if (info.estado === 400 && info.detalles.length > 0) {
    return {
      mensaje: 'Hay datos con un formato incorrecto.',
      detalles: info.detalles.map(traducirDetalle),
      sugerencia: 'Corrígelos y vuelve a enviar el formulario.',
    };
  }
  if (info.estado === 400) {
    return {
      mensaje: 'No pudimos validar tus datos con el Departamento de Defensa.',
      sugerencia:
        'Revisa que identificación, nombres, apellidos, fuerza, rango e ID de fuerza coincidan exactamente con tu registro oficial.',
    };
  }
  return null;
}

function describirLogin(info: InfoError): ErrorUsuario | null {
  if (info.tipo === 'http' && info.estado === 401) {
    return {
      mensaje: 'Identificación o contraseña incorrectas.',
      sugerencia:
        'Usa la contraseña de 6 caracteres que llegó a tu correo al registrarte. ' +
        'Después de 6 intentos fallidos seguidos, la identificación se bloquea 15 minutos.',
    };
  }
  return null;
}

function describirGeneral(info: InfoError, contexto: ContextoError): ErrorUsuario {
  const accion = contexto === 'catalogo' ? 'cargar la lista de fuerzas y rangos' : null;
  switch (info.tipo) {
    case 'red':
      return {
        mensaje: accion ? `No se pudo ${accion}: no hay conexión con el servidor.` : 'No hay conexión con el servidor.',
        sugerencia: 'Revisa tu conexión a internet y que el servidor esté encendido.',
      };
    case 'timeout':
      return {
        mensaje: 'El servidor tardó demasiado en responder.',
        sugerencia: 'Espera unos segundos y reintenta. Si sigue igual, revisa tu conexión.',
      };
    case 'respuesta':
      return {
        mensaje: 'El servidor respondió algo que la app no entiende.',
        sugerencia: 'Reintenta. Si el problema continúa, avisa al equipo de soporte.',
      };
    case 'http':
      break;
  }
  const { estado, codigo } = info;
  if (estado === 503 || codigo === 'INTEGRACION_PENDIENTE') {
    return { mensaje: 'Esta función no está disponible en el servidor por ahora.', sugerencia: REINTENTAR_MAS_TARDE };
  }
  if (estado === 401) {
    return { mensaje: 'Tu sesión venció.', sugerencia: 'Inicia sesión de nuevo.', accion: 'ir-a-login' };
  }
  if (estado === 403) {
    return {
      mensaje: 'Tu perfil no tiene permiso para esta acción.',
      sugerencia: 'Si crees que es un error, avisa a un administrador.',
    };
  }
  if (estado !== null && estado >= 500) {
    return { mensaje: 'El servidor tuvo un problema.', sugerencia: REINTENTAR_MAS_TARDE };
  }
  if (estado === 404) {
    return { mensaje: 'No se encontró lo que buscabas.', sugerencia: 'Revisa el dato e inténtalo de nuevo.' };
  }
  return { mensaje: 'No se pudo completar la acción.', sugerencia: 'Revisa los datos e inténtalo de nuevo.' };
}

/** Mensaje para la persona según el error y la pantalla donde ocurrió. */
export function describirError(info: InfoError | null, contexto: ContextoError = 'general'): ErrorUsuario {
  if (info === null) {
    return { mensaje: 'Ocurrió un error inesperado.', sugerencia: 'Intenta de nuevo.' };
  }
  const especifico =
    contexto === 'registro' ? describirRegistro(info) : contexto === 'login' ? describirLogin(info) : null;
  return especifico ?? describirGeneral(info, contexto);
}
