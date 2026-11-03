import { Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Boton } from '../components/Boton';
import { Pantalla } from '../components/Pantalla';
import { useAuth } from '../context/AuthContext';
import type { AccesoStackParamList } from '../navigation/tipos';

type Props = NativeStackScreenProps<AccesoStackParamList, 'Login'>;

export function LoginScreen({ navigation }: Props) {
  const { entrarDemo } = useAuth();
  return (
    <Pantalla>
      <Text>[PROVISIONAL] Pantalla de inicio de sesión sin conectar.</Text>
      <Boton titulo="Entrar (demo, sin API)" onPress={entrarDemo} />
      <Boton titulo="Registrarme" variante="secundario" onPress={() => navigation.navigate('Registro')} />
    </Pantalla>
  );
}
