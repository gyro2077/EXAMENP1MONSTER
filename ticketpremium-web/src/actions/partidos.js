'use server';

import { listarPartidos, listarLocalidades } from '@/lib/soapClient';

export async function getPartidosAction() {
  try {
    const partidos = await listarPartidos();

    if (!partidos.length) {
      return { success: false, message: 'No se encontraron partidos', data: [] };
    }

    return { success: true, data: partidos };
  } catch (error) {
    console.error('Error getting partidos:', error);
    return { success: false, message: 'Error al obtener los partidos', data: [] };
  }
}

export async function getLocalidadesAction(codigoPartido) {
  if (!codigoPartido) {
    return { success: false, message: 'Codigo de partido requerido', data: [] };
  }

  try {
    const localidades = await listarLocalidades(parseInt(codigoPartido));

    if (!localidades.length) {
      return { success: false, message: 'No se encontraron localidades', data: [] };
    }

    return { success: true, data: localidades };
  } catch (error) {
    console.error('Error getting localidades:', error);
    return { success: false, message: 'Error al obtener las localidades', data: [] };
  }
}
