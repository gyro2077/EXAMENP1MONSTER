'use server';

import { comprarBoletos as soapComprarBoletos } from '@/lib/soapClient';

let lastCompraData = null;

export async function comprarBoletosAction(formData) {
  const username = formData.get('username');
  const password = formData.get('password');
  const codigoPartido = parseInt(formData.get('codigoPartido'));
  const codigoLocalidad = formData.get('codigoLocalidad');
  const cantidad = parseInt(formData.get('cantidad'));

  if (!username || !password || !codigoPartido || !codigoLocalidad || !cantidad) {
    return { success: false, message: 'Todos los campos son requeridos' };
  }

  if (cantidad < 1) {
    return { success: false, message: 'La cantidad debe ser al menos 1' };
  }

  try {
    const result = await soapComprarBoletos(username, password, codigoPartido, codigoLocalidad, cantidad);

    if (result.exitoso) {
      lastCompraData = result;
      return { success: true, data: result };
    }

    return { success: false, message: result.mensaje || 'Error en la compra' };
  } catch (error) {
    console.error('Error buying tickets:', error);
    return { success: false, message: 'Error de conexion al procesar la compra' };
  }
}

export async function getLastCompraData() {
  return lastCompraData;
}
