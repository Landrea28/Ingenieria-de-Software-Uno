import { useCallback, useEffect, useState } from 'react';
import { obtenerFuerzasRangos } from '../api/servicios';
import { mensajeDeError } from '../api/mensajes';
import type { Fuerza } from '../types/api';

export type EstadoCatalogo =
  | { tipo: 'cargando' }
  | { tipo: 'listo'; fuerzas: Fuerza[] }
  | { tipo: 'error'; mensaje: string };

/** Carga el catálogo de fuerzas y rangos desde el backend. */
export function useCatalogo() {
  const [estado, setEstado] = useState<EstadoCatalogo>({ tipo: 'cargando' });

  const cargar = useCallback(async () => {
    setEstado({ tipo: 'cargando' });
    try {
      setEstado({ tipo: 'listo', fuerzas: await obtenerFuerzasRangos() });
    } catch (error) {
      setEstado({ tipo: 'error', mensaje: mensajeDeError(error) });
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  return { estado, recargar: cargar };
}
