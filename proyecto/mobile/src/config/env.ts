const modoApi: ModoApi = process.env.EXPO_PUBLIC_API_MODO === 'mock' ? 'mock' : 'real';
const urlReal = process.env.EXPO_PUBLIC_API_URL ?? 'http://10.0.2.2:8080';
const urlMock = process.env.EXPO_PUBLIC_API_MOCK_URL ?? 'http://10.0.2.2:4010';
const timeout = Number(process.env.EXPO_PUBLIC_API_TIMEOUT_MS ?? 10000);

export type ModoApi = 'real' | 'mock';

export const env = {
  API_URL: (modoApi === 'mock' ? urlMock : urlReal).replace(/\/+$/, ''),
  API_TIMEOUT_MS: Number.isFinite(timeout) && timeout > 0 ? timeout : 10000,
  MODO_API: modoApi,
};
