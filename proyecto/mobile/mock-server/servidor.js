// Servidor FALSO para desarrollar la app móvil sin depender del backend.
// Respeta el contrato OpenAPI (rutas, cuerpos y formato de error ErrorResponse).
// NO es el backend real: todo lo que responde es simulado y vive en memoria.
//
// Uso:  npm run mock        (puerto 4010; cambiar con MOCK_PORT)
//
// Casos de prueba incluidos:
//   Login        8000000001..8000000004 con contraseña "Ab123c"  -> 200 con token
//                cualquier otra combinación                        -> 401
//   Registro     identificación 8000000001..4 (simulados de Defensa) -> 201 la 1.ª vez, 409 después
//                8000000503                                          -> 503 INTEGRACION_PENDIENTE
//                otra identificación                                 -> 400 (no figura en Defensa)
//   Antecedentes 9000000001..19 con el mismo estado del seed; 9000000020 -> 404 SIN_INFORMACION;
//                toda identificación sin antecedente -> 404 SIN_INFORMACION
//                9000000500 -> 500; 9000000600 -> tarda 15 s (timeout)
//                Sin encabezado Authorization -> 401

const http = require('node:http');

const PUERTO = Number(process.env.MOCK_PORT || 4010);

const FUERZAS = [
  { codigo: 'POLICIA', nombre: 'Policia Nacional', rangos: ['Patrullero', 'Subintendente', 'Intendente'] },
  { codigo: 'EJERCITO', nombre: 'Ejercito Nacional', rangos: ['Soldado', 'Cabo', 'Sargento'] },
  { codigo: 'ARMADA', nombre: 'Armada Nacional', rangos: ['Marinero', 'Cabo Primero', 'Suboficial'] },
  { codigo: 'FUERZA_AEREA', nombre: 'Fuerza Aerea Colombiana', rangos: ['Aerotecnico', 'Tecnico Primero', 'Suboficial'] },
];
let siguienteRango = 1;
const CATALOGO = FUERZAS.map((f) => ({
  codigo: f.codigo,
  nombre: f.nombre,
  rangos: f.rangos.map((nombre) => ({ id: siguienteRango++, nombre })),
}));

const REQUERIDOS = new Set([3, 6, 9, 12, 15, 18]); // como el seed del backend (perfil demo)
const PERSONAL_DEFENSA = new Set(['8000000001', '8000000002', '8000000003', '8000000004']);
const registrados = new Set();
const correosRegistrados = new Set();

const SOLO_DIGITOS = /^[0-9]{6,10}$/;
const CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function error(res, estado, codigo, mensaje, detalles = []) {
  enviar(res, estado, { codigo, mensaje, detalles });
}

function enviar(res, estado, cuerpo) {
  if (cuerpo === undefined) {
    res.writeHead(estado);
    return res.end();
  }
  res.writeHead(estado, { 'Content-Type': 'application/json; charset=utf-8' });
  res.end(JSON.stringify(cuerpo));
}

function leerJson(req) {
  return new Promise((resolver) => {
    let datos = '';
    req.on('data', (parte) => (datos += parte));
    req.on('end', () => {
      try {
        resolver(JSON.parse(datos || '{}'));
      } catch {
        resolver(null);
      }
    });
  });
}

function validarRegistro(c) {
  const d = [];
  if (typeof c.identificacion !== 'string' || !SOLO_DIGITOS.test(c.identificacion)) d.push('identificacion: debe tener entre 6 y 10 digitos');
  if (typeof c.nombres !== 'string' || !c.nombres.trim() || c.nombres.length > 100) d.push('nombres: obligatorio, maximo 100 caracteres');
  if (typeof c.apellidos !== 'string' || !c.apellidos.trim() || c.apellidos.length > 100) d.push('apellidos: obligatorio, maximo 100 caracteres');
  if (typeof c.fuerzaCodigo !== 'string' || !c.fuerzaCodigo.trim()) d.push('fuerzaCodigo: obligatorio');
  if (!Number.isInteger(c.rangoId)) d.push('rangoId: obligatorio');
  if (typeof c.idFuerza !== 'string' || !c.idFuerza.trim() || c.idFuerza.length > 40) d.push('idFuerza: obligatorio, maximo 40 caracteres');
  if (typeof c.correo !== 'string' || !c.correo.trim() || !CORREO.test(c.correo) || c.correo.length > 200) d.push('correo: formato invalido');
  return d;
}

async function atender(req, res) {
  const { pathname } = new URL(req.url, 'http://localhost');
  const clave = `${req.method} ${pathname}`;
  console.log(new Date().toLocaleTimeString(), clave);

  if (clave === 'GET /api/health') return enviar(res, 200, { estado: 'OK' });

  if (clave === 'GET /api/catalogos/fuerzas-rangos') return enviar(res, 200, CATALOGO);

  if (clave === 'POST /api/personal/registro') {
    const c = await leerJson(req);
    if (c === null) return error(res, 400, 'VALIDACION', 'Peticion invalida');
    const detalles = validarRegistro(c);
    if (detalles.length) return error(res, 400, 'VALIDACION', 'Datos invalidos', detalles);
    const fuerza = CATALOGO.find((f) => f.codigo === c.fuerzaCodigo);
    if (!fuerza || !fuerza.rangos.some((r) => r.id === c.rangoId)) {
      return error(res, 400, 'VALIDACION', 'La fuerza y el rango no coinciden');
    }
    if (c.identificacion === '8000000503') {
      return error(res, 503, 'INTEGRACION_PENDIENTE', 'Registro pendiente de integracion con seguridad');
    }
    if (!PERSONAL_DEFENSA.has(c.identificacion)) {
      return error(res, 400, 'VALIDACION', 'El personal no figura en la validacion simulada de Defensa');
    }
    const correoNormalizado = c.correo.toLowerCase();
    if (registrados.has(c.identificacion) || correosRegistrados.has(correoNormalizado)) {
      return error(res, 409, 'DUPLICADO', 'El personal ya se encuentra registrado');
    }
    registrados.add(c.identificacion);
    correosRegistrados.add(correoNormalizado);
    return enviar(res, 201, { mensaje: 'Registro aceptado. Revise el correo de confirmacion' });
  }

  if (clave === 'POST /api/auth/login') {
    const c = await leerJson(req);
    if (c === null || typeof c.identificacion !== 'string' || !SOLO_DIGITOS.test(c.identificacion) ||
      typeof c.contrasena !== 'string' || !c.contrasena.trim() ||
        typeof c.recordarContrasena !== 'boolean') {
      return error(res, 400, 'VALIDACION', 'Datos invalidos');
    }
    if (PERSONAL_DEFENSA.has(c.identificacion) && c.contrasena === 'Ab123c') {
      const segundosExpiracion = Number(
        process.env.MOCK_EXPIRA_SEGUNDOS ?? (c.recordarContrasena ? 28800 : 900),
      );
      const expiraEnSegundos = Number.isFinite(segundosExpiracion) && segundosExpiracion > 0
        ? segundosExpiracion
        : c.recordarContrasena ? 28800 : 900;
      const exp = Math.floor(Date.now() / 1000) + expiraEnSegundos;
      return enviar(res, 200, {
        token: `mock.${Buffer.from(JSON.stringify({ exp })).toString('base64url')}.firma-simulada`,
        tipo: 'Bearer',
        expiraEnSegundos,
      });
    }
    return error(res, 401, 'NO_AUTENTICADO', 'Credenciales inválidas');
  }

  if (clave === 'POST /api/auth/logout') {
    if (!req.headers.authorization) return error(res, 401, 'NO_AUTENTICADO', 'Se requiere iniciar sesion');
    return enviar(res, 204);
  }

  const antecedente = pathname.match(/^\/api\/antecedentes\/([^/]+)$/);
  if (req.method === 'GET' && antecedente) {
    if (!req.headers.authorization) return error(res, 401, 'NO_AUTENTICADO', 'Se requiere iniciar sesion');
    const id = antecedente[1];
    if (!SOLO_DIGITOS.test(id)) return error(res, 400, 'VALIDACION', 'Peticion invalida');
    if (id === '9000000500') return error(res, 500, 'ERROR_INTERNO', 'Error inesperado del servidor');
    if (id === '9000000600') return setTimeout(() => enviar(res, 200, { identificacion: id, estado: 'NO_REQUERIDO', fuente: 'SIMULADA', fechaActualizacion: new Date().toISOString() }), 15000);
    if (id === '9000000020') return error(res, 404, 'SIN_INFORMACION', 'No hay informacion de antecedentes para la identificacion');
    const numero = Number(id) - 9000000000;
    if (numero >= 1 && numero <= 19) {
      return enviar(res, 200, {
        identificacion: id,
        estado: REQUERIDOS.has(numero) ? 'REQUERIDO' : 'NO_REQUERIDO',
        fuente: 'SIMULADA',
        fechaActualizacion: new Date().toISOString(),
      });
    }
    return error(res, 404, 'SIN_INFORMACION', 'No hay informacion de antecedentes para la identificacion');
  }

  return error(res, 404, 'NO_ENCONTRADO', 'Recurso no encontrado');
}

http
  .createServer((req, res) => {
    atender(req, res).catch(() => error(res, 500, 'ERROR_INTERNO', 'Error inesperado del servidor'));
  })
  .listen(PUERTO, '0.0.0.0', () => {
    console.log(`Servidor FALSO escuchando en http://0.0.0.0:${PUERTO} (no es el backend real)`);
  });
