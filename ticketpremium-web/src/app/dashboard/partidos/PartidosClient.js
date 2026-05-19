'use client';

import { useState } from 'react';
import Link from 'next/link';
import styles from './partidos.module.css';

export default function PartidosClient({ partidos }) {
  const [search, setSearch] = useState('');

  const filtered = partidos.filter((p) => {
    const term = search.toLowerCase();
    return (
      p.equipoLocal.toLowerCase().includes(term) ||
      p.equipoVisita.toLowerCase().includes(term) ||
      p.lugar.toLowerCase().includes(term)
    );
  });

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>
          Partidos <span className="text-gradient">Disponibles</span>
        </h1>
        <p className={styles.subtitle}>
          Selecciona un partido para ver localidades y comprar boletos
        </p>
      </div>

      <div className={styles.searchWrapper}>
        <input
          type="search"
          placeholder="Buscar equipo o estadio..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className={styles.searchInput}
        />
        {search && (
          <span className={styles.searchCount}>
            {filtered.length} resultado(s)
          </span>
        )}
      </div>

      {!partidos.length ? (
        <div className={styles.empty}>
          <p>No hay partidos disponibles</p>
        </div>
      ) : filtered.length === 0 ? (
        <div className={styles.empty}>
          <p>No se encontraron partidos para &quot;{search}&quot;</p>
        </div>
      ) : (
        <div className={styles.grid}>
          {filtered.map((partido, index) => (
            <Link
              key={partido.codigo}
              href={'/dashboard/partidos/' + partido.codigo}
              className={styles.card + ' animate-slideUp'}
              style={{ animationDelay: (index * 0.1) + 's' }}
            >
              <div className={styles.cardHeader}>
                <span className={styles.badge}>En vivo pronto</span>
              </div>
              <div className={styles.teams}>
                <div className={styles.team}>
                  <span className={styles.teamName}>{partido.equipoLocal}</span>
                  <span className={styles.teamLabel}>Local</span>
                </div>
                <span className={styles.vs}>VS</span>
                <div className={styles.team}>
                  <span className={styles.teamName}>{partido.equipoVisita}</span>
                  <span className={styles.teamLabel}>Visita</span>
                </div>
              </div>
              <div className={styles.cardFooter}>
                <div className={styles.info}>
                  <span className={styles.stadium}>{partido.lugar}</span>
                  <span className={styles.date}>{formatDate(partido.fecha)}</span>
                </div>
                <span className={styles.arrow}>{'>'}</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

function formatDate(dateStr) {
  if (!dateStr) return 'Por definir';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-EC', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return dateStr;
  }
}
