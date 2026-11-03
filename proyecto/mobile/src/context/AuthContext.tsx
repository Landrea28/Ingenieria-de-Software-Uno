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
  /** Aviso no sensible sobre persistencia segura o estado de la sesión. */
  avisoSesion: string | null;
  /** Lanza ApiError si las credenciales son inválidas o hay un fallo de red. */
  iniciarSesion: (identificacion: string, contrasena: string, recordar: boolean) => Promise<void>;
  cerrarSesion: () => Promise<void>;
}

const AuthContext = createContext<ValorAuth | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [estado, setEstado] = useState<EstadoSesion>('cargando');
  const [mensajeSesion, setMensajeSesion] = useState<string | null>(null);
  const [avisoSesion, setAvisoSesion] = useState<string | null>(null);
  const [expiraEn, setExpiraEn] = useState<number | null>(null);

  const expirarSesion = useCallback(() => {
    fijarToken(null);
    setExpiraEn(null);
    void borrarSesion();
    setMensajeSesion('Tu sesión venció. Inicia sesión de nuevo.');
    setEstado('sin-sesion');
  }, []);

  useEffect(() => {
    // Ante un 401 con token (sesión vencida o revocada) se vuelve al login.
    registrarAlExpirarSesion(expirarSesion);

    // Al abrir la app: si hay token guardado y no ha vencido, se restaura la sesión.
    // (Aún no existe un endpoint para validar el token; si el servidor lo rechaza,
    // el primer 401 devuelve al usuario al login.)
    let activo = true;
    void leerSesion().then((guardada) => {
      if (!activo) return;
      if (guardada) {
        fijarToken(guardada.token);
        setExpiraEn(guardada.expiraEn);
        setEstado('con-sesion');
      } else {
        setEstado('sin-sesion');
      }
    });
    return () => {
      activo = false;
      registrarAlExpirarSesion(null);
    };
  }, [expirarSesion]);

  // El servidor puede no recibir nuevas peticiones; por eso el token también
  // se hace caducar localmente al llegar su hora, aunque solo viva en memoria.
  useEffect(() => {
    if (estado !== 'con-sesion' || expiraEn === null) return;
    const restante = expiraEn - Date.now();
    if (restante <= 0) {
      expirarSesion();
      return;
    }
    const temporizador = setTimeout(expirarSesion, restante);
    return () => clearTimeout(temporizador);
  }, [estado, expiraEn, expirarSesion]);

  const iniciarSesion = useCallback(
    async (identificacion: string, contrasena: string, recordar: boolean) => {
      const respuesta = await iniciarSesionApi({
        identificacion,
        contrasena,
        recordarContrasena: recordar,
      });
      if (!respuesta?.token || respuesta.tipo !== 'Bearer') {
        throw new ApiError('respuesta', 200, null, 'Respuesta inesperada del servidor');
      }
      if (
        typeof respuesta.expiraEnSegundos !== 'number' ||
        !Number.isFinite(respuesta.expiraEnSegundos) ||
        respuesta.expiraEnSegundos <= 0
      ) {
        throw new ApiError('respuesta', 200, null, 'El servidor no informó un vencimiento válido para la sesión');
      }
      const vencimiento = calcularExpiracion(respuesta.expiraEnSegundos);
      fijarToken(respuesta.token); // en memoria siempre
      setExpiraEn(vencimiento);
      setAvisoSesion(null);
      if (recordar) {
        // Solo con la opción activa se persiste, y únicamente el token (nunca la contraseña).
        try {
          await guardarSesion(respuesta.token, vencimiento);
        } catch {
          await borrarSesion();
          setAvisoSesion('La sesión está activa, pero no se pudo guardar de forma segura para la próxima apertura.');
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
    setExpiraEn(null);
    await borrarSesion();
    setMensajeSesion(null);
    setAvisoSesion(null);
    setEstado('sin-sesion');
  }, []);

  const valor = useMemo(
    () => ({ estado, mensajeSesion, avisoSesion, iniciarSesion, cerrarSesion }),
    [estado, mensajeSesion, avisoSesion, iniciarSesion, cerrarSesion],
  );
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth(): ValorAuth {
  const contexto = useContext(AuthContext);
  if (!contexto) throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  return contexto;
}
