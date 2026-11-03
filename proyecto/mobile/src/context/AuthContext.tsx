import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';

// Versión inicial: la sesión vive solo en memoria y no hay login real todavía.
// Más adelante se conecta con la API y con el almacenamiento seguro.

type EstadoSesion = 'sin-sesion' | 'con-sesion';

interface ValorAuth {
  estado: EstadoSesion;
  /** PROVISIONAL: entra sin credenciales para poder navegar el flujo principal. */
  entrarDemo: () => void;
  cerrarSesion: () => void;
}

const AuthContext = createContext<ValorAuth | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [estado, setEstado] = useState<EstadoSesion>('sin-sesion');
  const entrarDemo = useCallback(() => setEstado('con-sesion'), []);
  const cerrarSesion = useCallback(() => setEstado('sin-sesion'), []);
  const valor = useMemo(() => ({ estado, entrarDemo, cerrarSesion }), [estado, entrarDemo, cerrarSesion]);
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth(): ValorAuth {
  const contexto = useContext(AuthContext);
  if (!contexto) throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  return contexto;
}
