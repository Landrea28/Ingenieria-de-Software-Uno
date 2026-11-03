/** Instante (ms desde epoch) en que vence un token que dura `segundos` desde `ahora`. */
export function calcularExpiracion(segundos: number, ahora: number = Date.now()): number {
  return ahora + segundos * 1000;
}

/** Lee el vencimiento `exp` de un JWT sin validar su firma (eso corresponde al backend). */
export function leerExpiracionJwt(token: string): number | null {
  const segmento = token.split('.')[1];
  if (!segmento) return null;

  try {
    const base64 = segmento.replace(/-/g, '+').replace(/_/g, '/');
    const base64ConRelleno = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    const payload = JSON.parse(atob(base64ConRelleno)) as { exp?: unknown };
    return typeof payload.exp === 'number' && Number.isFinite(payload.exp)
      ? payload.exp * 1000
      : null;
  } catch {
    return null;
  }
}

/** Un token sigue vigente mientras `expiraEn` sea posterior a `ahora` (con un pequeño margen). */
export function estaVigente(expiraEn: number, ahora: number = Date.now(), margenMs = 5000): boolean {
  return Number.isFinite(expiraEn) && expiraEn - margenMs > ahora;
}
