import { useCallback, useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { ApiError } from '../api/cliente';
import { mensajeDeError } from '../api/mensajes';
import { consultarAntecedente } from '../api/servicios';
import { Boton } from '../components/Boton';
import { Cargando, MensajeError } from '../components/Estados';
import { Pantalla } from '../components/Pantalla';
import type { PrincipalStackParamList } from '../navigation/tipos';
import { colores, espacio } from '../theme/colores';
import type { AntecedenteResponse } from '../types/api';

type Props = NativeStackScreenProps<PrincipalStackParamList, 'Resultado'>;

type EstadoConsulta =
  | { tipo: 'cargando' }
  | { tipo: 'ok'; datos: AntecedenteResponse }
  | { tipo: 'sin-informacion' }
  | { tipo: 'error'; mensaje: string };

export function ResultadoScreen({ route, navigation }: Props) {
  const { identificacion } = route.params;
  const [estado, setEstado] = useState<EstadoConsulta>({ tipo: 'cargando' });

  const consultar = useCallback(async () => {
    setEstado({ tipo: 'cargando' });
    try {
      const datos = await consultarAntecedente(identificacion);
      setEstado({ tipo: 'ok', datos });
    } catch (error) {
      // 404 SIN_INFORMACION es una respuesta válida del dominio (nunca equivale
      // a NO_REQUERIDO), no un fallo de la consulta.
      if (error instanceof ApiError && error.codigo === 'SIN_INFORMACION') {
        setEstado({ tipo: 'sin-informacion' });
        return;
      }
      setEstado({ tipo: 'error', mensaje: mensajeDeError(error) });
    }
  }, [identificacion]);

  useEffect(() => {
    void consultar();
  }, [consultar]);

  const esRequerido = estado.tipo === 'ok' && estado.datos.estado === 'REQUERIDO';

  return (
    <Pantalla>
      <Text style={estilos.identificacion}>Identificación: {identificacion}</Text>

      {estado.tipo === 'cargando' && <Cargando mensaje="Consultando antecedentes…" />}

      {estado.tipo === 'ok' && (
        <View
          style={[estilos.resultado, esRequerido ? estilos.fondoRequerido : estilos.fondoNoRequerido]}
          accessibilityRole="alert"
        >
          <Text style={[estilos.estadoTexto, { color: esRequerido ? colores.requerido : colores.noRequerido }]}>
            {esRequerido ? 'REQUERIDO' : 'NO REQUERIDO'}
          </Text>
          {estado.datos.fechaActualizacion && (
            <Text style={estilos.detalle}>Actualizado: {estado.datos.fechaActualizacion}</Text>
          )}
        </View>
      )}

      {estado.tipo === 'sin-informacion' && (
        <View style={estilos.aviso}>
          <Text style={estilos.textoAviso}>
            No hay información de antecedentes registrada para esta identificación. Esto no significa que la
            persona no esté requerida: consulta por otro medio si necesitas confirmarlo.
          </Text>
        </View>
      )}

      {estado.tipo === 'error' && <MensajeError mensaje={estado.mensaje} onReintentar={() => void consultar()} />}

      <Boton titulo="Escanear otra cédula" onPress={() => navigation.goBack()} />
      <Boton titulo="Volver al inicio" variante="secundario" onPress={() => navigation.popToTop()} />
    </Pantalla>
  );
}

const estilos = StyleSheet.create({
  identificacion: { fontSize: 15, color: colores.textoSuave },
  resultado: { borderRadius: 12, padding: espacio.l, alignItems: 'center', gap: espacio.s },
  fondoRequerido: { backgroundColor: colores.errorFondo },
  fondoNoRequerido: { backgroundColor: colores.exitoFondo },
  estadoTexto: { fontSize: 28, fontWeight: '700' },
  detalle: { fontSize: 13, color: colores.textoSuave },
  aviso: { backgroundColor: colores.avisoFondo, borderRadius: 10, padding: espacio.m },
  textoAviso: { color: colores.aviso, fontSize: 14 },
});
