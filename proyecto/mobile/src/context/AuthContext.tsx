import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { ApiError, fijarToken, registrarAlExpirarSesion } from '../api/cliente';
import { cerrarSesionApi, iniciarSesionApi } from '../api/servicios';
import { borrarSesion, guardarSesion, leerSesion } from '../storage/sesion';
import { calcularExpiracion } from '../utils/tiempo';

type EstadoSesion = 'cargando' | 'sin-sesion' | 'con-sesion';

interface ValorAuth {
  estado: EstadoSesion;
  /** Texto para mostrar en el login cuando la sesión terminó sola (p. ej. venció). */
  mensajeSesion: string | null;
  /** Lanza ApiError si las credenciales son inválidas o hay un fallo de red. */
  iniciarSesion: (identificacion: string, contrasena: string, recordar: boolean) => Promise<void>;
  cerrarSesion: () => Promise<void>;
}

const AuthContext = createContext<ValorAuth | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [estado, setEstado] = useState<EstadoSesion>('cargando');
  const [mensajeSesion, setMensajeSesion] = useState<string | null>(null);

  useEffect(() => {
    // Ante un 401 con token (sesión vencida o revocada) se vuelve al login.
    registrarAlExpirarSesion(() => {
      fijarToken(null);
      void borrarSesion();
      setMensajeSesion('Tu sesión venció. Inicia sesión de nuevo.');
      setEstado('sin-sesion');
    });

    // Al abrir la app: si hay token guardado y no ha vencido, se restaura la sesión.
    // (Aún no existe un endpoint para validar el token; si el servidor lo rechaza,
    // el primer 401 devuelve al usuario al login.)
    let activo = true;
    void leerSesion().then((guardada) => {
      if (!activo) return;
      if (guardada) {
        fijarToken(guardada.token);
        setEstado('con-sesion');
      } else {
        setEstado('sin-sesion');
      }
    });
    return () => {
      activo = false;
      registrarAlExpirarSesion(null);
    };
  }, []);

  const iniciarSesion = useCallback(
    async (identificacion: string, contrasena: string, recordar: boolean) => {
      const respuesta = await iniciarSesionApi({
        identificacion,
        contrasena,
        recordarContrasena: recordar,
      });
      if (!respuesta?.token) {
        throw new ApiError('respuesta', 200, null, 'Respuesta inesperada del servidor');
      }
      fijarToken(respuesta.token); // en memoria siempre
      if (recordar) {
        // Solo con la opción activa se persiste, y únicamente el token (nunca la contraseña).
        try {
          await guardarSesion(respuesta.token, calcularExpiracion(respuesta.expiraEnSegundos));
        } catch {
          // si no se puede guardar, la sesión sigue en memoria y no se recuerda
        }
      } else {
        await borrarSesion();
      }
      setMensajeSesion(null);
      setEstado('con-sesion');
    },
    [],
  );

  const cerrarSesion = useCallback(async () => {
    try {
      await cerrarSesionApi(); // mejor esfuerzo: aunque falle, se cierra localmente
    } catch {
      // sin conexión o endpoint pendiente
    }
    fijarToken(null);
    await borrarSesion();
    setMensajeSesion(null);
    setEstado('sin-sesion');
  }, []);

  const valor = useMemo(
    () => ({ estado, mensajeSesion, iniciarSesion, cerrarSesion }),
    [estado, mensajeSesion, iniciarSesion, cerrarSesion],
  );
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth(): ValorAuth {
  const contexto = useContext(AuthContext);
  if (!contexto) throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  return contexto;
}
