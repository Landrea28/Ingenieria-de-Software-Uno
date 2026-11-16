import { useMemo, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { describirErrorApi } from '../api/mensajes';
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
import type { ErrorUsuario } from '../utils/errores';
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

/** Estados del envío: pendiente → aceptado, o error con su explicación para la persona. */
type EstadoEnvio =
  | { tipo: 'inactivo' }
  | { tipo: 'pendiente' }
  | { tipo: 'aceptado'; mensaje: string }
  | { tipo: 'error'; error: ErrorUsuario };

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
      setEnvio({ tipo: 'aceptado', mensaje: respuesta.mensaje ?? 'Registro aceptado.' });
    } catch (error) {
      setEnvio({ tipo: 'error', error: describirErrorApi(error, 'registro') });
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
            Tu contraseña de 6 caracteres llegará al correo que escribiste (revisa también el spam). La app nunca la
            muestra ni la guarda. Con ella y tu identificación podrás iniciar sesión.
          </Text>
          <Text style={estilos.nota}>
            Entorno de pruebas: la validación de identidad está simulada y el correo no sale a internet; lo recibe
            Mailpit en el computador del backend (http://localhost:8025).
          </Text>
        </View>
        <Boton titulo="Ir a iniciar sesión" onPress={() => navigation.popTo('Login')} />
      </Pantalla>
    );
  }

  if (estado.tipo === 'cargando') return <Pantalla><Cargando mensaje="Cargando fuerzas y rangos…" /></Pantalla>;
  if (estado.tipo === 'error') return <Pantalla><MensajeError error={estado.error} onReintentar={recargar} /></Pantalla>;

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
      {envio.tipo === 'error' && (
        <MensajeError
          error={envio.error}
          accion={
            envio.error.accion === 'ir-a-login'
              ? { titulo: 'Ir a iniciar sesión', onPress: () => navigation.popTo('Login') }
              : undefined
          }
        />
      )}
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
});
