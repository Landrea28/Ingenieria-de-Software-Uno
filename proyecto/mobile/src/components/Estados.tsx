import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { colores, espacio } from '../theme/colores';
import { env } from '../config/env';
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

/** Mensaje de error con opción de reintentar. */
export function MensajeError({ mensaje, onReintentar }: { mensaje: string; onReintentar?: () => void }) {
  return (
    <View style={estilos.error} accessibilityRole="alert">
      <Text style={estilos.textoError}>{mensaje}</Text>
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
  textoError: { color: colores.error, fontSize: 15 },
  aviso: { backgroundColor: colores.avisoFondo, borderRadius: 10, padding: espacio.s },
  textoAviso: { color: colores.aviso, fontSize: 13 },
});
