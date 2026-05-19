'use server';

import { listarPartidos, listarLocalidades } from '@/lib/soapClient';

export async function evaluadorScanAction() {
  try {
    const pts = await listarPartidos();

    const localidadesPorPartido = {};
    let totalLoc = 0;

    for (const p of pts) {
      const locs = await listarLocalidades(p.codigo);
      localidadesPorPartido[p.codigo] = locs;
      totalLoc += locs.length;
    }

    return {
      success: true,
      partidos: pts,
      totalLocalidades: totalLoc,
      localidadesPorPartido,
    };
  } catch (e) {
    console.error('Evaluador scan error:', e);
    return { success: false, error: e.message };
  }
}
