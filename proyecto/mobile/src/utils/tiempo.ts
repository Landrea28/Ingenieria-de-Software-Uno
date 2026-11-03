/** Instante (ms desde epoch) en que vence un token que dura `segundos` desde `ahora`. */
export function calcularExpiracion(segundos: number, ahora: number = Date.now()): number {
  return ahora + segundos * 1000;
}

/** Un token sigue vigente mientras `expiraEn` sea posterior a `ahora` (con un pequeño margen). */
export function estaVigente(expiraEn: number, ahora: number = Date.now(), margenMs = 5000): boolean {
  return Number.isFinite(expiraEn) && expiraEn - margenMs > ahora;
}
