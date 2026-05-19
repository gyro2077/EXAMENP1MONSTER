'use client';

import { useState, useEffect } from 'react';
import { getPartidosAction } from '@/actions/partidos';
import { getReporteAction } from '@/actions/reportes';
import styles from './reportes.module.css';

export default function ReportesPage() {
  const [partidos, setPartidos] = useState([]);
  const [selectedPartido, setSelectedPartido] = useState('');
  const [reporte, setReporte] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    async function fetchPartidos() {
      const result = await getPartidosAction();
      if (result.success) {
        setPartidos(result.data);
      }
    }
    fetchPartidos();
  }, []);

  useEffect(() => {
    if (!selectedPartido) {
      setReporte([]);
      setError('');
      return;
    }

    let cancelled = false;

    async function fetchReporte() {
      setLoading(true);
      setError('');
      const result = await getReporteAction(selectedPartido);

      if (!cancelled) {
        if (result.success) {
          setReporte(result.data);
        } else {
          setError(result.message);
          setReporte([]);
        }
        setLoading(false);
      }
    }

    fetchReporte();

    return () => {
      cancelled = true;
    };
  }, [selectedPartido]);

  const totalVendido = reporte.reduce((sum, r) => sum + (r.cantidadTotalVendida || 0), 0);
  const totalRecaudado = reporte.reduce((sum, r) => sum + (parseFloat(r.totalRecaudado) || 0), 0);

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>
          Reporte de <span className="text-gradient">Ventas</span>
        </h1>
        <p className={styles.subtitle}>
          Consulta las ventas y recaudacion por partido
        </p>
      </div>

      <div className={styles.selectWrapper}>
        <label htmlFor="partido">Seleccionar Partido</label>
        <select
          id="partido"
          value={selectedPartido}
          onChange={(e) => setSelectedPartido(e.target.value)}
        >
          <option value="">-- Elige un partido --</option>
          {partidos.map((p) => (
            <option key={p.codigo} value={p.codigo}>
              {p.equipoLocal} vs {p.equipoVisita}
            </option>
          ))}
        </select>
      </div>

      {error && <div className={styles.error}>{error}</div>}

      {loading && (
        <div className={styles.loading}>
          <div className={styles.loadingSpinner} />
          <p>Cargando reporte...</p>
        </div>
      )}

      {!loading && reporte.length > 0 && (
        <div className={`${styles.tableWrapper} animate-slideUp`}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Localidad</th>
                <th>Boletos Vendidos</th>
                <th>Total Recaudado</th>
              </tr>
            </thead>
            <tbody>
              {reporte.map((row, i) => (
                <tr key={i}>
                  <td className={styles.localidadCell}>{row.codigoLocalidad}</td>
                  <td>{row.cantidadTotalVendida}</td>
                  <td className="text-gold">$ {parseFloat(row.totalRecaudado).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td className={styles.totalCell}>Total</td>
                <td>{totalVendido}</td>
                <td className="text-gold">$ {totalRecaudado.toFixed(2)}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      )}

      {!loading && selectedPartido && reporte.length === 0 && !error && (
        <div className={styles.empty}>
          <p>No hay ventas registradas para este partido</p>
        </div>
      )}
    </div>
  );
}
