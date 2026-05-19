'use server';

import { doLogin } from '@/lib/soapClient';
import { cookies } from 'next/headers';

export async function loginAction(formData) {
  const username = formData.get('username');
  const password = formData.get('password');

  if (!username || !password) {
    return { success: false, message: 'Usuario y contraseña son requeridos' };
  }

  try {
    const result = await doLogin(username, password);

    if (result.exitoso) {
      const cookieStore = await cookies();
      cookieStore.set('session_user', username, { maxAge: 60 * 60 * 24, path: '/' });
      cookieStore.set('session_cliente_id', result.clienteId?.toString() || '', { maxAge: 60 * 60 * 24, path: '/' });
      return { success: true, message: result.mensaje, username: result.username };
    }

    return { success: false, message: result.mensaje || 'Credenciales invalidas' };
  } catch (error) {
    console.error('Login error:', error);
    return { success: false, message: 'Error de conexion con el servidor' };
  }
}

export async function logoutAction() {
  const cookieStore = await cookies();
  cookieStore.delete('session_user');
  cookieStore.delete('session_cliente_id');
  return { success: true };
}

export async function getSession() {
  const cookieStore = await cookies();
  const username = cookieStore.get('session_user')?.value;
  const clienteId = cookieStore.get('session_cliente_id')?.value;
  return { username, clienteId, isAuthenticated: !!username };
}
