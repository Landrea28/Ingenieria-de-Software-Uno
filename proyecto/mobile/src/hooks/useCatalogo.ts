import { useCallback, useEffect, useState } from 'react';
import { obtenerFuerzasRangos } from '../api/servicios';
import { describirErrorApi } from '../api/mensajes';
import type { Fuerza } from '../types/api';
import type { ErrorUsuario } from '../utils/errores';

export type EstadoCatalogo =
  | { tipo: 'cargando' }
  | { tipo: 'listo'; fuerzas: Fuerza[] }
  | { tipo: 'error'; error: ErrorUsuario };

/** Carga el catálogo de fuerzas y rangos desde el backend. */
export function useCatalogo() {
  const [estado, setEstado] = useState<EstadoCatalogo>({ tipo: 'cargando' });

  const cargar = useCallback(async () => {
    setEstado({ tipo: 'cargando' });
    try {
      setEstado({ tipo: 'listo', fuerzas: await obtenerFuerzasRangos() });
    } catch (error) {
      setEstado({ tipo: 'error', error: describirErrorApi(error, 'catalogo') });
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  return { estado, recargar: cargar };
}
