import { useState } from 'react';
import { StyleSheet, Switch, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { ApiError } from '../api/cliente';
import { mensajeDeError } from '../api/mensajes';
import { Boton } from '../components/Boton';
import { CampoTexto } from '../components/CampoTexto';
import { BannerModo, MensajeError } from '../components/Estados';
import { Pantalla } from '../components/Pantalla';
import { useAuth } from '../context/AuthContext';
import type { AccesoStackParamList } from '../navigation/tipos';
import { colores, espacio } from '../theme/colores';
import { validarIdentificacion } from '../utils/validaciones';

type Props = NativeStackScreenProps<AccesoStackParamList, 'Login'>;

export function LoginScreen({ navigation }: Props) {
  const { iniciarSesion, mensajeSesion } = useAuth();
  const [identificacion, setIdentificacion] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [recordar, setRecordar] = useState(false);
  const [errorId, setErrorId] = useState<string | undefined>();
  const [errorContrasena, setErrorContrasena] = useState<string | undefined>();
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);

  const entrar = async () => {
    const eId = validarIdentificacion(identificacion);
    const eClave = contrasena ? undefined : 'La contraseña es obligatoria';
    setErrorId(eId);
    setErrorContrasena(eClave);
    setErrorGeneral(null);
    if (eId || eClave) return;

    setCargando(true);
    try {
      await iniciarSesion(identificacion.trim(), contrasena, recordar);
      // Si todo sale bien, el navegador cambia solo al flujo principal.
    } catch (error) {
      setCargando(false);
      setContrasena('');
      setErrorGeneral(
        error instanceof ApiError && error.tipo === 'http' && error.estado === 401
          ? 'Identificación o contraseña incorrectas.'
          : mensajeDeError(error),
      );
    }
  };

  return (
    <Pantalla>
      <BannerModo />
      {mensajeSesion && (
        <View style={estilos.aviso}>
          <Text style={estilos.textoAviso}>{mensajeSesion}</Text>
        </View>
      )}
      <CampoTexto
        etiqueta="Identificación"
        keyboardType="number-pad"
        maxLength={10}
        value={identificacion}
        onChangeText={(v) => setIdentificacion(v.replace(/\D/g, ''))}
        error={errorId}
      />
      <CampoTexto
        etiqueta="Contraseña"
        secureTextEntry
        autoCapitalize="none"
        autoCorrect={false}
        maxLength={64}
        value={contrasena}
        onChangeText={setContrasena}
        error={errorContrasena}
      />
      <View style={estilos.fila}>
        <View style={estilos.filaTexto}>
          <Text style={estilos.etiqueta}>Recordar contraseña</Text>
          <Text style={estilos.ayuda}>
            Mantiene tu sesión abierta con un token cifrado. Tu contraseña no se guarda en el teléfono.
          </Text>
        </View>
        <Switch
          accessibilityLabel="Recordar contraseña"
          value={recordar}
          onValueChange={setRecordar}
        />
      </View>
      {errorGeneral && <MensajeError mensaje={errorGeneral} />}
      <Boton titulo="Iniciar sesión" cargando={cargando} onPress={() => void entrar()} />
      <Boton
        titulo="Registrarme"
        variante="secundario"
        deshabilitado={cargando}
        onPress={() => navigation.navigate('Registro')}
      />
    </Pantalla>
  );
}

const estilos = StyleSheet.create({
  fila: { flexDirection: 'row', alignItems: 'center', gap: espacio.m },
  filaTexto: { flex: 1, gap: 2 },
  etiqueta: { fontSize: 15, fontWeight: '600', color: colores.texto },
  ayuda: { fontSize: 12, color: colores.textoSuave },
  aviso: { backgroundColor: colores.avisoFondo, borderRadius: 10, padding: espacio.s },
  textoAviso: { color: colores.aviso, fontSize: 14 },
});
