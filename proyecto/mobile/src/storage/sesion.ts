import * as SecureStore from 'expo-secure-store';
import { estaVigente } from '../utils/tiempo';

// Almacenamiento seguro de la sesión (Keychain en iOS, Keystore en Android).
// REGLA: aquí solo se guarda el TOKEN que emite el backend y su fecha de expiración.
// La contraseña nunca se guarda: ni aquí, ni en AsyncStorage, ni en ningún otro lugar.

const CLAVE_TOKEN = 'sesion_token';
const CLAVE_EXPIRA = 'sesion_expira_en';

// "THIS_DEVICE_ONLY": el valor no se copia a otros dispositivos mediante respaldos,
// y solo se puede leer con el teléfono desbloqueado.
const OPCIONES: SecureStore.SecureStoreOptions = {
  keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
};

export interface SesionGuardada {
  token: string;
  expiraEn: number;
}

export async function guardarSesion(token: string, expiraEn: number): Promise<void> {
  if (!(await SecureStore.isAvailableAsync())) return;
  await SecureStore.setItemAsync(CLAVE_TOKEN, token, OPCIONES);
  await SecureStore.setItemAsync(CLAVE_EXPIRA, String(expiraEn), OPCIONES);
}

/** Devuelve la sesión guardada solo si existe y no ha vencido; si venció, la borra. */
export async function leerSesion(): Promise<SesionGuardada | null> {
  try {
    if (!(await SecureStore.isAvailableAsync())) return null;
    const token = await SecureStore.getItemAsync(CLAVE_TOKEN, OPCIONES);
    const expiraEn = Number(await SecureStore.getItemAsync(CLAVE_EXPIRA, OPCIONES));
    if (!token || !estaVigente(expiraEn)) {
      await borrarSesion();
      return null;
    }
    return { token, expiraEn };
  } catch {
    return null; // si el almacenamiento falla, se pide iniciar sesión de nuevo
  }
}

export async function borrarSesion(): Promise<void> {
  try {
    await SecureStore.deleteItemAsync(CLAVE_TOKEN, OPCIONES);
    await SecureStore.deleteItemAsync(CLAVE_EXPIRA, OPCIONES);
  } catch {
    // nada que borrar o almacenamiento no disponible
  }
}
