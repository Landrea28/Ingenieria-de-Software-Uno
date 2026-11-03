import { useMemo, useState } from 'react';
import { Alert, Text } from 'react-native';
import { Boton } from '../components/Boton';
import { CampoTexto } from '../components/CampoTexto';
import { BannerModo, Cargando, MensajeError } from '../components/Estados';
import { Pantalla } from '../components/Pantalla';
import { Selector } from '../components/Selector';
import { useCatalogo } from '../hooks/useCatalogo';
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

export function RegistroScreen() {
  const { estado, recargar } = useCatalogo();
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

  const enviar = () => {
    setIntentado(true);
    const encontrados = validarRegistro(datos);
    setErrores(encontrados);
    if (Object.keys(encontrados).length > 0) return;
    // PROVISIONAL: aún no se envía a la API.
    Alert.alert('Formulario válido', 'Todavía no está conectado con el backend.');
  };

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
      <Boton titulo="Registrarme" onPress={enviar} />
    </Pantalla>
  );
}
