import type { CodigoFuerza } from '../types/api';

// Validaciones del formulario de registro. Son una ayuda de UX (avisar antes de
// enviar); la validación que manda es la del backend, que se vuelve a ejecutar allí.

export interface DatosRegistro {
  identificacion: string;
  nombres: string;
  apellidos: string;
  fuerzaCodigo: CodigoFuerza | null;
  rangoId: number | null;
  idFuerza: string;
  correo: string;
}

export type ErroresRegistro = Partial<Record<keyof DatosRegistro, string>>;

const SOLO_DIGITOS = /^[0-9]{6,10}$/;
const SOLO_LETRAS = /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]+$/;
const CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validarIdentificacion(valor: string): string | undefined {
  const v = valor.trim();
  if (!v) return 'La identificación es obligatoria';
  if (!SOLO_DIGITOS.test(v)) return 'Debe tener solo números, entre 6 y 10 dígitos';
  return undefined;
}

function validarNombre(valor: string, campo: string): string | undefined {
  const v = valor.trim();
  if (!v) return `${campo} es obligatorio`;
  if (v.length > 100) return `${campo} admite máximo 100 caracteres`;
  if (!SOLO_LETRAS.test(v)) return `${campo} solo admite letras y espacios`;
  return undefined;
}

export function validarCorreo(valor: string): string | undefined {
  const v = valor.trim();
  if (!v) return 'El correo es obligatorio';
  if (v.length > 200) return 'El correo admite máximo 200 caracteres';
  if (!CORREO.test(v)) return 'El formato del correo no es válido';
  return undefined;
}

export function validarRegistro(d: DatosRegistro): ErroresRegistro {
  const errores: ErroresRegistro = {
    identificacion: validarIdentificacion(d.identificacion),
    nombres: validarNombre(d.nombres, 'Los nombres'),
    apellidos: validarNombre(d.apellidos, 'Los apellidos'),
    fuerzaCodigo: d.fuerzaCodigo ? undefined : 'Selecciona la fuerza pública',
    rangoId: d.rangoId !== null ? undefined : 'Selecciona el rango',
    idFuerza: !d.idFuerza.trim()
      ? 'El ID de fuerza es obligatorio'
      : d.idFuerza.trim().length > 40
        ? 'El ID de fuerza admite máximo 40 caracteres'
        : undefined,
    correo: validarCorreo(d.correo),
  };
  // Quita las claves sin error para que `Object.keys(errores).length === 0` signifique "válido".
  for (const clave of Object.keys(errores) as (keyof DatosRegistro)[]) {
    if (errores[clave] === undefined) delete errores[clave];
  }
  return errores;
}
