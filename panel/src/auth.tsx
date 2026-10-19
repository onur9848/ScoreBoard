import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { createApi, setUnauthorizedHandler, storedKey, storeKey, type Api } from './api/client';

interface AuthState {
  api: Api | null;
  signIn: (key: string) => void;
  signOut: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [key, setKey] = useState<string | null>(storedKey);

  const signIn = useCallback((value: string) => {
    storeKey(value);
    setKey(value);
  }, []);
  const signOut = useCallback(() => {
    storeKey(null);
    setKey(null);
  }, []);

  useEffect(() => setUnauthorizedHandler(signOut), [signOut]);

  const value = useMemo(() => ({ api: key ? createApi(key) : null, signIn, signOut }), [key, signIn, signOut]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const state = useContext(AuthContext);
  if (!state) throw new Error('AuthProvider eksik');
  return state;
}

/** Giriş yapılmış sayfalarda API istemcisi. */
export function useApi(): Api {
  const { api } = useAuth();
  if (!api) throw new Error('Giriş yapılmadı');
  return api;
}
