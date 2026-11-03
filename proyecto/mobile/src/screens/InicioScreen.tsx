import { Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Boton } from '../components/Boton';
import { Pantalla } from '../components/Pantalla';
import { useAuth } from '../context/AuthContext';
import type { PrincipalStackParamList } from '../navigation/tipos';

type Props = NativeStackScreenProps<PrincipalStackParamList, 'Inicio'>;

export function InicioScreen({ navigation }: Props) {
  const { cerrarSesion } = useAuth();
  return (
    <Pantalla>
      <Text>Consulta el estado de antecedentes de un ciudadano.</Text>
      <Boton titulo="Escanear cédula" onPress={() => navigation.navigate('Escaner')} />
      <Boton titulo="Cerrar sesión" variante="secundario" onPress={cerrarSesion} />
    </Pantalla>
  );
}
