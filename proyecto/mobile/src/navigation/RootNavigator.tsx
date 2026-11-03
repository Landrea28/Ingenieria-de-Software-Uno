import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Cargando } from '../components/Estados';
import { Pantalla } from '../components/Pantalla';
import { useAuth } from '../context/AuthContext';
import { EscanerScreen } from '../screens/EscanerScreen';
import { InicioScreen } from '../screens/InicioScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { RegistroScreen } from '../screens/RegistroScreen';
import { ResultadoScreen } from '../screens/ResultadoScreen';
import type { AccesoStackParamList, PrincipalStackParamList } from './tipos';

const Acceso = createNativeStackNavigator<AccesoStackParamList>();
const Principal = createNativeStackNavigator<PrincipalStackParamList>();

function FlujoAcceso() {
  return (
    <Acceso.Navigator initialRouteName="Login">
      <Acceso.Screen name="Login" component={LoginScreen} options={{ title: 'Iniciar sesión' }} />
      <Acceso.Screen name="Registro" component={RegistroScreen} options={{ title: 'Registro de personal' }} />
    </Acceso.Navigator>
  );
}

function FlujoPrincipal() {
  return (
    <Principal.Navigator initialRouteName="Inicio">
      <Principal.Screen name="Inicio" component={InicioScreen} options={{ title: 'Consulta Ciudadano' }} />
      <Principal.Screen name="Escaner" component={EscanerScreen} options={{ title: 'Escanear cédula' }} />
      <Principal.Screen name="Resultado" component={ResultadoScreen} options={{ title: 'Resultado' }} />
    </Principal.Navigator>
  );
}

/**
 * El navegador se elige según la sesión: sin sesión solo existen las pantallas
 * de acceso; con sesión solo las del flujo principal. Al cambiar el estado,
 * React Navigation reemplaza el flujo completo (no se puede "volver" al login).
 */
export function RootNavigator() {
  const { estado } = useAuth();
  // Mientras se revisa si hay una sesión guardada no se muestra ningún flujo.
  if (estado === 'cargando') {
    return (
      <Pantalla>
        <Cargando mensaje="Revisando sesión…" />
      </Pantalla>
    );
  }
  return (
    <NavigationContainer>
      {estado === 'con-sesion' ? <FlujoPrincipal /> : <FlujoAcceso />}
    </NavigationContainer>
  );
}
