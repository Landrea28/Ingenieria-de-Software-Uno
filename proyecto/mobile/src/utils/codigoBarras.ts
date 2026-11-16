// Extracción de la identificación a partir de los datos crudos del código de
// barras. Ver docs/supuestos.md (S-10) para el supuesto sobre el formato real
// del símbolo de la cédula y cómo se validó con códigos de prueba.

const SEPARADORES = /[\s.-]/g;
const IDENTIFICACION_VALIDA = /^[0-9]{6,10}$/;

/**
 * Devuelve la identificación si los datos leídos, tras quitar espacios y
 * separadores comunes, son una cadena de 6 a 10 dígitos. Devuelve `null`
 * cuando el símbolo no se pudo interpretar como una identificación válida
 * (código ilegible, de otro tipo de documento, o dañado).
 */
export function extraerIdentificacion(datosCrudos: string): string | null {
  const limpio = datosCrudos.trim().replace(SEPARADORES, '');
  return IDENTIFICACION_VALIDA.test(limpio) ? limpio : null;
}
