import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { colores, espacio } from '../theme/colores';
import { env } from '../config/env';
import type { ErrorUsuario } from '../utils/errores';
import { Boton } from './Boton';

/** Indicador de carga reutilizable. */
export function Cargando({ mensaje = 'Cargando…' }: { mensaje?: string }) {
  return (
    <View style={estilos.centro} accessibilityLiveRegion="polite">
      <ActivityIndicator size="large" color={colores.primario} />
      <Text style={estilos.textoSuave}>{mensaje}</Text>
    </View>
  );
}

interface PropsMensajeError {
  error: ErrorUsuario;
  onReintentar?: () => void;
  /** Botón de apoyo, por ejemplo «Ir a iniciar sesión» cuando la cuenta ya existe. */
  accion?: { titulo: string; onPress: () => void };
}

/** Error con tres partes: qué pasó, qué datos fallaron y qué puede hacer la persona. */
export function MensajeError({ error, onReintentar, accion }: PropsMensajeError) {
  return (
    <View style={estilos.error} accessibilityRole="alert">
      <Text style={estilos.tituloError}>{error.mensaje}</Text>
      {error.detalles?.map((detalle) => (
        <Text key={detalle} style={estilos.textoError}>
          • {detalle}
        </Text>
      ))}
      {error.sugerencia && <Text style={estilos.sugerencia}>{error.sugerencia}</Text>}
      {accion && <Boton titulo={accion.titulo} onPress={accion.onPress} />}
      {onReintentar && <Boton titulo="Reintentar" variante="secundario" onPress={onReintentar} />}
    </View>
  );
}

/** Aviso visible cuando la app habla con el servidor falso, no con el backend real. */
export function BannerModo() {
  if (env.MODO_API !== 'mock') return null;
  return (
    <View style={estilos.aviso}>
      <Text style={estilos.textoAviso}>
        Modo simulado: las respuestas vienen de un servidor falso, no del backend real.
      </Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  centro: { alignItems: 'center', gap: espacio.s, padding: espacio.l },
  textoSuave: { color: colores.textoSuave },
  error: {
    backgroundColor: colores.errorFondo,
    borderRadius: 10,
    padding: espacio.m,
    gap: espacio.s,
  },
  tituloError: { color: colores.error, fontSize: 16, fontWeight: '700' },
  textoError: { color: colores.error, fontSize: 15 },
  sugerencia: { color: colores.texto, fontSize: 14 },
  aviso: { backgroundColor: colores.avisoFondo, borderRadius: 10, padding: espacio.s },
  textoAviso: { color: colores.aviso, fontSize: 13 },
});
