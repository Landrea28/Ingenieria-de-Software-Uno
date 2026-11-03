import { Picker } from '@react-native-picker/picker';
import { StyleSheet, Text, View } from 'react-native';
import { colores } from '../theme/colores';

export interface OpcionSelector<T extends string | number> {
  etiqueta: string;
  valor: T;
}

interface Props<T extends string | number> {
  etiqueta: string;
  valor: T | null;
  opciones: OpcionSelector<T>[];
  onCambio: (valor: T | null) => void;
  placeholder?: string;
  deshabilitado?: boolean;
  error?: string;
}

/** Lista desplegable. El valor `null` representa "sin selección". */
export function Selector<T extends string | number>({
  etiqueta,
  valor,
  opciones,
  onCambio,
  placeholder = 'Selecciona una opción',
  deshabilitado,
  error,
}: Props<T>) {
  return (
    <View style={estilos.contenedor}>
      <Text style={estilos.etiqueta}>{etiqueta}</Text>
      <View style={[estilos.caja, !!error && estilos.cajaError, deshabilitado && estilos.inactivo]}>
        <Picker
          accessibilityLabel={etiqueta}
          enabled={!deshabilitado}
          selectedValue={valor ?? ''}
          onValueChange={(v) => onCambio(v === '' ? null : (v as T))}
          style={estilos.picker}
        >
          <Picker.Item label={placeholder} value="" color={colores.textoSuave} />
          {opciones.map((o) => (
            <Picker.Item key={String(o.valor)} label={o.etiqueta} value={o.valor} />
          ))}
        </Picker>
      </View>
      {!!error && <Text style={estilos.error}>{error}</Text>}
    </View>
  );
}

const estilos = StyleSheet.create({
  contenedor: { gap: 4 },
  etiqueta: { fontSize: 14, fontWeight: '600', color: colores.texto },
  caja: {
    borderWidth: 1,
    borderColor: colores.borde,
    borderRadius: 10,
    backgroundColor: colores.superficie,
    overflow: 'hidden',
  },
  cajaError: { borderColor: colores.error },
  inactivo: { opacity: 0.5 },
  picker: { color: colores.texto },
  error: { color: colores.error, fontSize: 13 },
});
