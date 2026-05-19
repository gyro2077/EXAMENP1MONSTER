'use client';

import { useState, useEffect, use } from 'react';
import { useRouter } from 'next/navigation';
import { getLocalidadesAction } from '@/actions/partidos';
import { comprarBoletosAction } from '@/actions/compra';
import styles from './detalle.module.css';

export default function DetallePartidoPage({ params }) {
  const { id } = use(params);
  const router = useRouter();
  const [localidades, setLocalidades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedLocalidad, setSelectedLocalidad] = useState(null);
  const [cantidad, setCantidad] = useState(1);
  const [comprando, setComprando] = useState(false);
  const [compraError, setCompraError] = useState('');

  useEffect(() => {
    async function fetchLocalidades() {
      const result = await getLocalidadesAction(id);
      if (result.success) {
        const disponibles = result.data.filter(l => l.disponibilidad > 0);
        setLocalidades(disponibles);
      } else {
        setError(result.message);
      }
      setLoading(false);
    }
    fetchLocalidades();
  }, [id]);

  const subtotal = selectedLocalidad ? selectedLocalidad.precio * cantidad : 0;
  const iva = subtotal * 0.15;
  const total = subtotal + iva;

  async function handleCompra(e) {
    e.preventDefault();
    setComprando(true);
    setCompraError('');

    const formData = new FormData();
    formData.append('username', 'MONSTER');
    formData.append('password', 'MONSTER9');
    formData.append('codigoPartido', id);
    formData.append('codigoLocalidad', selectedLocalidad.codigoLocalidad);
    formData.append('cantidad', cantidad);

    const result = await comprarBoletosAction(formData);

    if (result.success) {
      router.push(`/dashboard/factura/${result.data.facturaId}`);
    } else {
      setCompraError(result.message);
    }

    setComprando(false);
  }

  if (loading) {
    return (
      <div className={styles.loading}>
        <div className={styles.spinner} />
        <p>Cargando localidades...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <button onClick={() => router.back()} className={styles.backBtn}>
        ← Volver
      </button>

      <div className={styles.header}>
        <h1 className={styles.title}>Selecciona tu Localidad</h1>
        <p className={styles.subtitle}>Partido #{id}</p>
      </div>

      {error && (
        <div className={styles.error}>{error}</div>
      )}

      {!localidades.length ? (
        <div className={styles.empty}>
          <p>No hay localidades disponibles para este partido</p>
        </div>
      ) : (
        <div className={styles.layout}>
          <div className={styles.localidades}>
            {localidades.map((loc) => (
              <button
                key={loc.codigoLocalidad}
                onClick={() => {
                  setSelectedLocalidad(loc);
                  setCantidad(1);
                }}
                className={`${styles.localidadCard} ${selectedLocalidad?.codigoLocalidad === loc.codigoLocalidad ? styles.selected : ''}`}
              >
                <div className={styles.locInfo}>
                  <span className={styles.locName}>{loc.codigoLocalidad}</span>
                  <span className={styles.locDisponibilidad}>
                    {loc.disponibilidad} disponibles
                  </span>
                </div>
                <span className={styles.locPrecio}>$ {parseFloat(loc.precio).toFixed(2)}</span>
              </button>
            ))}
          </div>

          {selectedLocalidad && (
            <div className={`${styles.resumen} animate-slideUp`}>
              <h2 className={styles.resumenTitle}>Resumen de Compra</h2>

              <div className={styles.resumenDetails}>
                <div className={styles.detailRow}>
                  <span>Localidad</span>
                  <span className="text-gradient">{selectedLocalidad.codigoLocalidad}</span>
                </div>
                <div className={styles.detailRow}>
                  <span>Precio unitario</span>
                  <span>$ {parseFloat(selectedLocalidad.precio).toFixed(2)}</span>
                </div>

                <div className={styles.quantitySelector}>
                  <span>Cantidad</span>
                  <div className={styles.quantityControls}>
                    <button
                      type="button"
                      onClick={() => setCantidad(Math.max(1, cantidad - 1))}
                      className={styles.qtyBtn}
                    >
                      -
                    </button>
                    <span className={styles.qtyValue}>{cantidad}</span>
                    <button
                      type="button"
                      onClick={() => setCantidad(Math.min(selectedLocalidad.disponibilidad, cantidad + 1))}
                      className={styles.qtyBtn}
                    >
                      +
                    </button>
                  </div>
                </div>

                <div className={styles.divider} />

                <div className={styles.detailRow}>
                  <span>Subtotal</span>
                  <span>$ {subtotal.toFixed(2)}</span>
                </div>
                <div className={styles.detailRow}>
                  <span>IVA (15%)</span>
                  <span>$ {iva.toFixed(2)}</span>
                </div>
                <div className={`${styles.detailRow} ${styles.totalRow}`}>
                  <span>Total</span>
                  <span className="text-gold">$ {total.toFixed(2)}</span>
                </div>
              </div>

              {compraError && (
                <div className={styles.error}>{compraError}</div>
              )}

              <form onSubmit={handleCompra}>
                <input type="hidden" name="codigoPartido" value={id} />
                <input type="hidden" name="codigoLocalidad" value={selectedLocalidad.codigoLocalidad} />
                <input type="hidden" name="cantidad" value={cantidad} />
                <button type="submit" className="btn-primary" disabled={comprando}>
                  {comprando ? 'Procesando...' : 'Confirmar Compra'}
                </button>
              </form>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
