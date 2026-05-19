'use server';

import { generarReporte } from '@/lib/soapClient';

export async function getReporteAction(codigoPartido) {
  if (!codigoPartido) {
    return { success: false, message: 'Codigo de partido requerido', data: [] };
  }

  try {
    const reporte = await generarReporte(parseInt(codigoPartido));

    if (!reporte.length) {
      return { success: true, message: 'No hay ventas registradas', data: [] };
    }

    return { success: true, data: reporte };
  } catch (error) {
    console.error('Error getting reporte:', error);
    return { success: false, message: 'Error al obtener el reporte', data: [] };
  }
}
