import { StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Boton } from '../components/Boton';
import { Pantalla } from '../components/Pantalla';
import { useAuth } from '../context/AuthContext';
import type { PrincipalStackParamList } from '../navigation/tipos';
import { colores, espacio } from '../theme/colores';

type Props = NativeStackScreenProps<PrincipalStackParamList, 'Inicio'>;

export function InicioScreen({ navigation }: Props) {
  const { avisoSesion, cerrarSesion } = useAuth();
  return (
    <Pantalla>
      <Text>Consulta el estado de antecedentes de un ciudadano.</Text>
      {avisoSesion && (
        <View style={estilos.aviso} accessibilityRole="alert">
          <Text style={estilos.textoAviso}>{avisoSesion}</Text>
        </View>
      )}
      <Boton titulo="Escanear cédula" onPress={() => navigation.navigate('Escaner')} />
      <Boton titulo="Cerrar sesión" variante="secundario" onPress={() => void cerrarSesion()} />
    </Pantalla>
  );
}

const estilos = StyleSheet.create({
  aviso: { backgroundColor: colores.avisoFondo, borderRadius: 10, padding: espacio.m },
  textoAviso: { color: colores.aviso, fontSize: 14 },
});
