const urlBase = process.env.EXPO_PUBLIC_API_URL ?? 'http://IP:8080';
const timeout = Number(process.env.EXPO_PUBLIC_API_TIMEOUT_MS ?? 10000);

export type ModoApi = 'real' | 'mock';

export const env = {
  API_URL: urlBase.replace(/\/+$/, ''),
  API_TIMEOUT_MS: Number.isFinite(timeout) && timeout > 0 ? timeout : 10000,
  MODO_API: (process.env.EXPO_PUBLIC_API_MODO === 'mock' ? 'mock' : 'real') as ModoApi,
};
