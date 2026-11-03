import { Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Pantalla } from '../components/Pantalla';
import type { PrincipalStackParamList } from '../navigation/tipos';

type Props = NativeStackScreenProps<PrincipalStackParamList, 'Resultado'>;

export function ResultadoScreen({ route }: Props) {
  return (
    <Pantalla>
      <Text>[PROVISIONAL] Resultado para la identificación {route.params.identificacion}.</Text>
    </Pantalla>
  );
}
