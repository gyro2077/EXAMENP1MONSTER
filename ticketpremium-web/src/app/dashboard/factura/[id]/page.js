import { getLastCompraData } from '@/actions/compra';
import Link from 'next/link';
import styles from './factura.module.css';

export const dynamic = 'force-dynamic';

function formatDateTime(dateStr) {
  if (!dateStr) return 'N/A';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-EC', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return dateStr;
  }
}

export default async function FacturaPage() {
  const data = await getLastCompraData();

  if (!data) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>
          <p>No se encontro informacion de la factura. Realiza una compra primero.</p>
          <Link href="/dashboard/partidos" className="btn-primary">
            Ver Partidos
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.success}>
        <div className={styles.checkIcon}>✓</div>
        <h1 className={styles.title}>¡Compra Exitosa!</h1>
        <p className={styles.subtitle}>Tu boleto ha sido confirmado</p>
      </div>

      <div className={styles.ticket}>
        <div className={styles.ticketHeader}>
          <span className={styles.ticketIcon}>⚽</span>
          <div>
            <h2 className={styles.ticketTitle}>TicketPremium</h2>
            <p className={styles.ticketSubtitle}>Comprobante de Compra</p>
          </div>
        </div>

        <div className={styles.ticketBody}>
          <div className={styles.ticketSection}>
            <h3 className={styles.sectionTitle}>Datos del Cliente</h3>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Cliente</span>
              <span className={styles.value}>{data.clienteNombre}</span>
            </div>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Cedula</span>
              <span className={styles.value}>{data.clienteCedula}</span>
            </div>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Factura Nro.</span>
              <span className={styles.value}>#{String(data.facturaId).padStart(6, '0')}</span>
            </div>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Fecha</span>
              <span className={styles.value}>{formatDateTime(data.fechaCompra)}</span>
            </div>
          </div>

          <div className={styles.ticketSection}>
            <h3 className={styles.sectionTitle}>Detalle del Evento</h3>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Partido</span>
              <span className={styles.value}>{data.nombrePartido}</span>
            </div>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Localidad</span>
              <span className="text-gradient">{data.codigoLocalidad}</span>
            </div>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Cantidad</span>
              <span className={styles.value}>{data.cantidad} boleto(s)</span>
            </div>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Precio Unitario</span>
              <span className={styles.value}>$ {data.precioUnitario.toFixed(2)}</span>
            </div>
          </div>

          <div className={styles.ticketSection}>
            <h3 className={styles.sectionTitle}>Desglose de Valores</h3>
            <div className={styles.ticketRow}>
              <span className={styles.label}>Base Imponible (Subtotal)</span>
              <span className={styles.value}>$ {data.subtotal.toFixed(2)}</span>
            </div>
            <div className={styles.ticketRow}>
              <span className={styles.label}>IVA (15%)</span>
              <span className={styles.value}>$ {data.iva.toFixed(2)}</span>
            </div>
            <div className={styles.divider} />
            <div className={`${styles.ticketRow} ${styles.totalRow}`}>
              <span className={styles.totalLabel}>VALOR TOTAL A PAGAR</span>
              <span className="text-gold">$ {data.total.toFixed(2)}</span>
            </div>
          </div>
        </div>

        <div className={styles.ticketFooter}>
          <p>Gracias por su compra - TicketPremium</p>
        </div>
      </div>

      <div className={styles.actions}>
        <Link href="/dashboard/partidos" className="btn-primary">
          Comprar mas boletos
        </Link>
        <Link href="/dashboard/reportes" className="btn-secondary">
          Ver Reportes
        </Link>
      </div>
    </div>
  );
}
