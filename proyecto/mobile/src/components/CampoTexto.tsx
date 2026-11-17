import { StyleSheet, Text, TextInput, View, type TextInputProps } from 'react-native';
import { colores } from '../theme/colores';

interface Props extends TextInputProps {
  etiqueta: string;
  error?: string;
}

export function CampoTexto({ etiqueta, error, style, ...resto }: Props) {
  return (
    <View style={estilos.contenedor}>
      <Text style={estilos.etiqueta}>{etiqueta}</Text>
      <TextInput
        accessibilityLabel={etiqueta}
        placeholderTextColor={colores.textoSuave}
        style={[estilos.entrada, !!error && estilos.entradaError, style]}
        {...resto}
      />
      {!!error && <Text style={estilos.error}>{error}</Text>}
    </View>
  );
}

const estilos = StyleSheet.create({
  contenedor: { gap: 4 },
  etiqueta: { fontSize: 14, fontWeight: '600', color: colores.texto },
  entrada: {
    minHeight: 48,
    borderWidth: 1,
    borderColor: colores.borde,
    borderRadius: 10,
    paddingHorizontal: 12,
    fontSize: 16,
    color: colores.texto,
    backgroundColor: colores.superficie,
  },
  entradaError: { borderColor: colores.error },
  error: { color: colores.error, fontSize: 13 },
});
