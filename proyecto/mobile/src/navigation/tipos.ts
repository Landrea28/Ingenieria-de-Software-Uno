// Rutas y parámetros de cada flujo de la app.

/** Flujo de acceso: lo que ve quien NO tiene sesión. */
export type AccesoStackParamList = {
  Login: undefined;
  Registro: undefined;
};

/** Flujo principal: lo que ve quien SÍ tiene sesión. */
export type PrincipalStackParamList = {
  Inicio: undefined;
  Escaner: undefined;
  Resultado: { identificacion: string };
};
