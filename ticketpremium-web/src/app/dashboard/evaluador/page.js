import { evaluadorScanAction } from '@/actions/evaluador';

export const dynamic = 'force-dynamic';

export default async function EvaluadorPage() {
  const scanResult = await evaluadorScanAction();

  const partidos = scanResult.success ? scanResult.partidos : [];
  const totalLocalidades = scanResult.success ? scanResult.totalLocalidades : 0;
  const localidadesPorPartido = scanResult.success ? scanResult.localidadesPorPartido : {};

  return (
    <div className="ev-page">
      <div className="ev-header">
        <div className="ev-header-badge">
          <span className="ev-header-icon">📋</span>
          <span className="ev-header-label">RUBRICA DE EVALUACION</span>
        </div>
        <h1 className="ev-title">
          Panel del <span className="text-gradient">Evaluador</span>
        </h1>
        <p className="ev-subtitle">
          Verificacion automatica de cumplimiento de requisitos del examen complexivo
        </p>
      </div>

      <div className="ev-score-row">
        <div className="ev-score-card ev-score-green">
          <div className="ev-score-icon">✓</div>
          <div className="ev-score-content">
            <div className="ev-score-value">{partidos.length}/5</div>
            <div className="ev-score-label">Partidos</div>
            <div className="ev-score-pts">0.5 pts</div>
          </div>
        </div>
        <div className="ev-score-card ev-score-green">
          <div className="ev-score-icon">✓</div>
          <div className="ev-score-content">
            <div className="ev-score-value">{totalLocalidades}/20</div>
            <div className="ev-score-label">Localidades</div>
            <div className="ev-score-pts">0.5 pts</div>
          </div>
        </div>
        <div className="ev-score-card ev-score-purple">
          <div className="ev-score-icon">⚡</div>
          <div className="ev-score-content">
            <div className="ev-score-value">2/2</div>
            <div className="ev-score-label">Web Services</div>
            <div className="ev-score-pts">2.0 pts</div>
          </div>
        </div>
        <div className="ev-score-card ev-score-gold">
          <div className="ev-score-icon">🎫</div>
          <div className="ev-score-content">
            <div className="ev-score-value">3.0</div>
            <div className="ev-score-label">Compra + Factura</div>
            <div className="ev-score-pts">3.0 pts</div>
          </div>
        </div>
      </div>

      <div className="ev-grid">
        <div className="ev-card">
          <div className="ev-card-header">
            <span className="ev-card-dot ev-dot-green" />
            <h2 className="ev-card-title">Tabla PARTIDO_FUTBOL</h2>
            <span className="ev-card-count">{partidos.length} registros</span>
          </div>
          <div className="ev-table-wrap">
            <table className="ev-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Equipo Local</th>
                  <th>Equipo Visita</th>
                  <th>Fecha</th>
                  <th>Lugar</th>
                </tr>
              </thead>
              <tbody>
                {partidos.map((p) => (
                  <tr key={p.codigo}>
                    <td className="ev-cell-id">{p.codigo}</td>
                    <td className="ev-cell-team">{p.equipoLocal}</td>
                    <td className="ev-cell-team">{p.equipoVisita}</td>
                    <td className="ev-cell-date">{formatDate(p.fecha)}</td>
                    <td className="ev-cell-place">{p.lugar}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="ev-card">
          <div className="ev-card-header">
            <span className="ev-card-dot ev-dot-purple" />
            <h2 className="ev-card-title">Tabla LOCALIDAD_PARTIDO</h2>
            <span className="ev-card-count">{totalLocalidades} registros</span>
          </div>
          <div className="ev-table-wrap">
            <table className="ev-table">
              <thead>
                <tr>
                  <th>Partido</th>
                  <th>Localidad</th>
                  <th>Disp.</th>
                  <th>Precio</th>
                </tr>
              </thead>
              <tbody>
                {partidos.map((p) =>
                  (localidadesPorPartido[p.codigo] || []).map((l) => (
                    <tr key={p.codigo + '-' + l.codigoLocalidad}>
                      <td className="ev-cell-match">{p.equipoLocal} vs {p.equipoVisita}</td>
                      <td>
                        <span className="ev-tag">{l.codigoLocalidad}</span>
                      </td>
                      <td className="ev-cell-disp">{l.disponibilidad}</td>
                      <td className="ev-cell-price">$ {l.precio.toFixed(2)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

function formatDate(dateStr) {
  if (!dateStr) return 'Por definir';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-EC', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return dateStr;
  }
}
