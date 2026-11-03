import { ActivityIndicator, Pressable, StyleSheet, Text } from 'react-native';
import { colores } from '../theme/colores';

interface Props {
  titulo: string;
  onPress: () => void;
  variante?: 'primario' | 'secundario';
  cargando?: boolean;
  deshabilitado?: boolean;
}

export function Boton({ titulo, onPress, variante = 'primario', cargando, deshabilitado }: Props) {
  const inactivo = deshabilitado || cargando;
  const primario = variante === 'primario';
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ disabled: !!inactivo, busy: !!cargando }}
      disabled={inactivo}
      onPress={onPress}
      style={({ pressed }) => [
        estilos.base,
        primario ? estilos.primario : estilos.secundario,
        inactivo && estilos.inactivo,
        pressed && estilos.presionado,
      ]}
    >
      {cargando ? (
        <ActivityIndicator color={primario ? colores.primarioTexto : colores.primario} />
      ) : (
        <Text style={[estilos.texto, !primario && estilos.textoSecundario]}>{titulo}</Text>
      )}
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  base: {
    minHeight: 48,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 16,
  },
  primario: { backgroundColor: colores.primario },
  secundario: { backgroundColor: colores.superficie, borderWidth: 1, borderColor: colores.primario },
  inactivo: { opacity: 0.5 },
  presionado: { opacity: 0.8 },
  texto: { color: colores.primarioTexto, fontSize: 16, fontWeight: '600' },
  textoSecundario: { color: colores.primario },
});
