import { useCallback, useRef, useState } from 'react';
import { Linking, StyleSheet, Text, View } from 'react-native';
import { CameraView, useCameraPermissions, type BarcodeScanningResult } from 'expo-camera';
import { useFocusEffect } from '@react-navigation/native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Boton } from '../components/Boton';
import { CampoTexto } from '../components/CampoTexto';
import { Cargando } from '../components/Estados';
import { Pantalla } from '../components/Pantalla';
import type { PrincipalStackParamList } from '../navigation/tipos';
import { colores, espacio } from '../theme/colores';
import { extraerIdentificacion } from '../utils/codigoBarras';
import { validarIdentificacion } from '../utils/validaciones';

type Props = NativeStackScreenProps<PrincipalStackParamList, 'Escaner'>;

// Supuesto S-10 (docs/supuestos.md): la cédula codifica la identificación en
// un símbolo 1D; se admiten los formatos más comunes para ese uso.
const TIPOS_CODIGO = ['code128', 'pdf417', 'code39', 'code93', 'itf14', 'codabar'] as const;

type Estado = 'escaneando' | 'no-legible' | 'manual';

export function EscanerScreen({ navigation }: Props) {
  const [permiso, solicitarPermiso] = useCameraPermissions();
  const [estado, setEstado] = useState<Estado>('escaneando');
  const [idManual, setIdManual] = useState('');
  const [errorManual, setErrorManual] = useState<string | undefined>();
  const bloqueado = useRef(false);

  // Al volver de Resultado (o al entrar), se reanuda el escaneo.
  useFocusEffect(
    useCallback(() => {
      bloqueado.current = false;
      setEstado('escaneando');
    }, []),
  );

  const alLeer = (resultado: BarcodeScanningResult) => {
    if (bloqueado.current) return;
    const identificacion = extraerIdentificacion(resultado.data);
    if (!identificacion) {
      bloqueado.current = true;
      setEstado('no-legible');
      return;
    }
    bloqueado.current = true;
    navigation.navigate('Resultado', { identificacion });
  };

  const reintentar = () => {
    bloqueado.current = false;
    setEstado('escaneando');
  };

  const consultarManual = () => {
    const error = validarIdentificacion(idManual);
    setErrorManual(error);
    if (error) return;
    navigation.navigate('Resultado', { identificacion: idManual.trim() });
  };

  if (!permiso) {
    return (
      <Pantalla>
        <Cargando mensaje="Revisando permiso de cámara…" />
      </Pantalla>
    );
  }

  if (!permiso.granted) {
    return (
      <Pantalla>
        <Text style={estilos.texto}>
          Esta pantalla necesita acceso a la cámara para leer el código de barras de la cédula.
        </Text>
        {permiso.canAskAgain ? (
          <Boton titulo="Conceder permiso" onPress={() => void solicitarPermiso()} />
        ) : (
          <Text style={estilos.ayuda}>
            El permiso fue denegado. Activa la cámara para esta app desde los ajustes del sistema.
          </Text>
        )}
        {!permiso.canAskAgain && (
          <Boton titulo="Abrir ajustes" variante="secundario" onPress={() => void Linking.openSettings()} />
        )}
        <Text style={estilos.ayuda}>
          También puedes ingresar la identificación manualmente mientras resuelves el permiso.
        </Text>
        <CampoTexto
          etiqueta="Identificación"
          keyboardType="number-pad"
          maxLength={10}
          value={idManual}
          onChangeText={(v) => setIdManual(v.replace(/\D/g, ''))}
          error={errorManual}
        />
        <Boton titulo="Consultar" onPress={consultarManual} />
        <Boton titulo="Cancelar" variante="secundario" onPress={() => navigation.goBack()} />
      </Pantalla>
    );
  }

  return (
    <Pantalla desplazable={false}>
      <View style={estilos.contenedorCamara}>
        {estado === 'escaneando' && (
          <CameraView
            style={estilos.camara}
            facing="back"
            barcodeScannerSettings={{ barcodeTypes: [...TIPOS_CODIGO] }}
            onBarcodeScanned={alLeer}
          />
        )}
        {estado === 'escaneando' && (
          <View style={estilos.superposicion} pointerEvents="none">
            <View style={estilos.marco} />
            <Text style={estilos.instruccion}>Apunta la cámara al código de barras de la cédula</Text>
          </View>
        )}
        {estado === 'no-legible' && (
          <View style={estilos.centro}>
            <Text style={estilos.texto}>No se pudo leer el código. Intenta de nuevo con buena luz y sin reflejos.</Text>
            <Boton titulo="Reintentar" onPress={reintentar} />
          </View>
        )}
      </View>
      <View style={estilos.panel}>
        {estado === 'manual' ? (
          <>
            <CampoTexto
              etiqueta="Identificación"
              keyboardType="number-pad"
              maxLength={10}
              value={idManual}
              onChangeText={(v) => setIdManual(v.replace(/\D/g, ''))}
              error={errorManual}
            />
            <Boton titulo="Consultar" onPress={consultarManual} />
            <Boton titulo="Volver a escanear" variante="secundario" onPress={() => setEstado('escaneando')} />
          </>
        ) : (
          <Boton titulo="Ingresar identificación manualmente" variante="secundario" onPress={() => setEstado('manual')} />
        )}
        <Boton titulo="Cancelar" variante="secundario" onPress={() => navigation.goBack()} />
      </View>
    </Pantalla>
  );
}

const estilos = StyleSheet.create({
  texto: { fontSize: 15, color: colores.texto },
  ayuda: { fontSize: 13, color: colores.textoSuave },
  contenedorCamara: { flex: 1, borderRadius: 12, overflow: 'hidden', backgroundColor: '#000' },
  camara: { flex: 1 },
  superposicion: {
    position: 'absolute',
    inset: 0,
    alignItems: 'center',
    justifyContent: 'center',
    gap: espacio.m,
  },
  marco: {
    width: '80%',
    height: 120,
    borderWidth: 2,
    borderColor: colores.primarioTexto,
    borderRadius: 10,
  },
  instruccion: {
    color: colores.primarioTexto,
    fontSize: 14,
    textAlign: 'center',
    paddingHorizontal: espacio.l,
  },
  centro: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: espacio.m, padding: espacio.l },
  panel: { padding: espacio.m, gap: espacio.s },
});
