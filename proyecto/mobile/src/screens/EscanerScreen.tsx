import { Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Boton } from '../components/Boton';
import { Pantalla } from '../components/Pantalla';
import type { PrincipalStackParamList } from '../navigation/tipos';

type Props = NativeStackScreenProps<PrincipalStackParamList, 'Escaner'>;

export function EscanerScreen({ navigation }: Props) {
  return (
    <Pantalla>
      <Text>[PROVISIONAL] Aquí irá la cámara.</Text>
      <Boton
        titulo="Simular lectura de 9000000001"
        onPress={() => navigation.navigate('Resultado', { identificacion: '9000000001' })}
      />
    </Pantalla>
  );
}
