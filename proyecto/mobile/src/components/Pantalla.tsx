import type { ReactNode } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colores, espacio } from '../theme/colores';

interface Props {
  children: ReactNode;
  /** false para pantallas que no deben hacer scroll (p. ej. la cámara). */
  desplazable?: boolean;
}

/** Contenedor común: respeta zonas seguras, teclado y scroll. */
export function Pantalla({ children, desplazable = true }: Props) {
  return (
    <SafeAreaView style={estilos.seguro}>
      <KeyboardAvoidingView
        style={estilos.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        {desplazable ? (
          <ScrollView
            contentContainerStyle={estilos.contenido}
            keyboardShouldPersistTaps="handled"
          >
            {children}
          </ScrollView>
        ) : (
          children
        )}
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  seguro: { flex: 1, backgroundColor: colores.fondo },
  flex: { flex: 1 },
  contenido: { padding: espacio.m, gap: espacio.m },
});
