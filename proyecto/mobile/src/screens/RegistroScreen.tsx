import { useMemo, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { ApiError } from '../api/cliente';
import { mensajeDeError } from '../api/mensajes';
import { registrarPersonal } from '../api/servicios';
import { Boton } from '../components/Boton';
import { CampoTexto } from '../components/CampoTexto';
import { BannerModo, Cargando, MensajeError } from '../components/Estados';
import { Pantalla } from '../components/Pantalla';
import { Selector } from '../components/Selector';
import { useCatalogo } from '../hooks/useCatalogo';
import type { AccesoStackParamList } from '../navigation/tipos';
import { colores, espacio } from '../theme/colores';
import type { CodigoFuerza } from '../types/api';
import { validarRegistro, type DatosRegistro, type ErroresRegistro } from '../utils/validaciones';

const VACIO: DatosRegistro = {
  identificacion: '',
  nombres: '',
  apellidos: '',
  fuerzaCodigo: null,
  rangoId: null,
  idFuerza: '',
  correo: '',
};

type Props = NativeStackScreenProps<AccesoStackParamList, 'Registro'>;

/** Estados del envío: pendiente → aceptado / rechazado / integración pendiente / error de red. */
type EstadoEnvio =
  | { tipo: 'inactivo' }
  | { tipo: 'pendiente' }
  | { tipo: 'aceptado'; mensaje: string }
  | { tipo: 'rechazado'; mensaje: string; detalles: string[] }
  | { tipo: 'integracion-pendiente' }
  | { tipo: 'error'; mensaje: string };

export function RegistroScreen({ navigation }: Props) {
  const { estado, recargar } = useCatalogo();
  const [envio, setEnvio] = useState<EstadoEnvio>({ tipo: 'inactivo' });
  const [datos, setDatos] = useState<DatosRegistro>(VACIO);
  const [errores, setErrores] = useState<ErroresRegistro>({});
  const [intentado, setIntentado] = useState(false);

  const fuerzas = estado.tipo === 'listo' ? estado.fuerzas : [];
  const rangos = useMemo(
    () => fuerzas.find((f) => f.codigo === datos.fuerzaCodigo)?.rangos ?? [],
    [fuerzas, datos.fuerzaCodigo],
  );

  // Una vez que el usuario intentó enviar, los errores se recalculan al escribir.
  const actualizar = (cambio: Partial<DatosRegistro>) => {
    const nuevos = { ...datos, ...cambio };
    setDatos(nuevos);
    if (intentado) setErrores(validarRegistro(nuevos));
  };

  const cambiarFuerza = (codigo: CodigoFuerza | null) =>
    // El rango depende de la fuerza: al cambiarla se descarta el rango elegido.
    actualizar({ fuerzaCodigo: codigo, rangoId: null });

  const enviar = async () => {
    setIntentado(true);
    const encontrados = validarRegistro(datos);
    setErrores(encontrados);
    if (Object.keys(encontrados).length > 0) return;
    if (datos.fuerzaCodigo === null || datos.rangoId === null) return; // ya cubierto por la validación

    setEnvio({ tipo: 'pendiente' });
    try {
      const respuesta = await registrarPersonal({
        identificacion: datos.identificacion.trim(),
        nombres: datos.nombres.trim(),
        apellidos: datos.apellidos.trim(),
        fuerzaCodigo: datos.fuerzaCodigo,
        rangoId: datos.rangoId,
        idFuerza: datos.idFuerza.trim(),
        correo: datos.correo.trim(),
      });
      setEnvio({ tipo: 'aceptado', mensaje: respuesta.mensaje });
    } catch (error) {
      if (error instanceof ApiError && error.tipo === 'http') {
        if (error.estado === 503 || error.codigo === 'INTEGRACION_PENDIENTE') {
          return setEnvio({ tipo: 'integracion-pendiente' });
        }
        if (error.estado === 400 || error.estado === 409) {
          // 400: datos inválidos o no figura en Defensa; 409: ya registrado.
          return setEnvio({ tipo: 'rechazado', mensaje: error.message, detalles: error.detalles });
        }
      }
      setEnvio({ tipo: 'error', mensaje: mensajeDeError(error) });
    }
  };

  if (envio.tipo === 'aceptado') {
    return (
      <Pantalla>
        <BannerModo />
        <View style={estilos.exito}>
          <Text style={estilos.tituloExito}>Registro aceptado</Text>
          <Text style={estilos.textoExito}>{envio.mensaje}</Text>
          <Text style={estilos.textoExito}>
            Tu contraseña de 6 caracteres llegará a tu correo. La app nunca la muestra ni la guarda.
          </Text>
          <Text style={estilos.nota}>
            La validación de identidad está simulada por el backend. La entrega del correo depende
            de la configuración SMTP del entorno.
          </Text>
        </View>
        <Boton titulo="Ir a iniciar sesión" onPress={() => navigation.navigate('Login')} />
      </Pantalla>
    );
  }

  if (estado.tipo === 'cargando') return <Pantalla><Cargando mensaje="Cargando fuerzas y rangos…" /></Pantalla>;
  if (estado.tipo === 'error') return <Pantalla><MensajeError mensaje={estado.mensaje} onReintentar={recargar} /></Pantalla>;

  return (
    <Pantalla>
      <BannerModo />
      <Text>Completa tus datos. La contraseña se enviará a tu correo.</Text>
      <CampoTexto
        etiqueta="Identificación"
        keyboardType="number-pad"
        maxLength={10}
        value={datos.identificacion}
        onChangeText={(v) => actualizar({ identificacion: v.replace(/\D/g, '') })}
        error={errores.identificacion}
      />
      <CampoTexto
        etiqueta="Nombres"
        autoCapitalize="words"
        maxLength={100}
        value={datos.nombres}
        onChangeText={(v) => actualizar({ nombres: v })}
        error={errores.nombres}
      />
      <CampoTexto
        etiqueta="Apellidos"
        autoCapitalize="words"
        maxLength={100}
        value={datos.apellidos}
        onChangeText={(v) => actualizar({ apellidos: v })}
        error={errores.apellidos}
      />
      <Selector<CodigoFuerza>
        etiqueta="Fuerza pública"
        valor={datos.fuerzaCodigo}
        opciones={fuerzas.map((f) => ({ etiqueta: f.nombre, valor: f.codigo }))}
        onCambio={cambiarFuerza}
        error={errores.fuerzaCodigo}
      />
      <Selector<number>
        etiqueta="Rango"
        valor={datos.rangoId}
        opciones={rangos.map((r) => ({ etiqueta: r.nombre, valor: r.id }))}
        onCambio={(id) => actualizar({ rangoId: id })}
        deshabilitado={datos.fuerzaCodigo === null}
        placeholder={datos.fuerzaCodigo === null ? 'Primero elige la fuerza' : 'Selecciona un rango'}
        error={errores.rangoId}
      />
      <CampoTexto
        etiqueta="ID de fuerza"
        autoCapitalize="characters"
        maxLength={40}
        value={datos.idFuerza}
        onChangeText={(v) => actualizar({ idFuerza: v })}
        error={errores.idFuerza}
      />
      <CampoTexto
        etiqueta="Correo electrónico"
        keyboardType="email-address"
        autoCapitalize="none"
        autoCorrect={false}
        maxLength={200}
        value={datos.correo}
        onChangeText={(v) => actualizar({ correo: v })}
        error={errores.correo}
      />
      {envio.tipo === 'rechazado' && (
        <View style={estilos.rechazo} accessibilityRole="alert">
          <Text style={estilos.textoRechazo}>Registro rechazado: {envio.mensaje}</Text>
          {envio.detalles.map((d) => (
            <Text key={d} style={estilos.textoRechazo}>• {d}</Text>
          ))}
        </View>
      )}
      {envio.tipo === 'integracion-pendiente' && (
        <View style={estilos.pendiente} accessibilityRole="alert">
          <Text style={estilos.textoPendiente}>
            El servidor aún no tiene habilitado el registro (integración pendiente). Intenta más tarde.
          </Text>
        </View>
      )}
      {envio.tipo === 'error' && <MensajeError mensaje={envio.mensaje} />}
      <Boton
        titulo={envio.tipo === 'pendiente' ? 'Enviando…' : 'Registrarme'}
        cargando={envio.tipo === 'pendiente'}
        onPress={() => void enviar()}
      />
    </Pantalla>
  );
}

const estilos = StyleSheet.create({
  exito: { backgroundColor: colores.exitoFondo, borderRadius: 10, padding: espacio.m, gap: espacio.s },
  tituloExito: { color: colores.exito, fontSize: 18, fontWeight: '700' },
  textoExito: { color: colores.texto, fontSize: 15 },
  nota: { color: colores.textoSuave, fontSize: 13 },
  rechazo: { backgroundColor: colores.errorFondo, borderRadius: 10, padding: espacio.m, gap: 4 },
  textoRechazo: { color: colores.error, fontSize: 14 },
  pendiente: { backgroundColor: colores.avisoFondo, borderRadius: 10, padding: espacio.m },
  textoPendiente: { color: colores.aviso, fontSize: 14 },
});
