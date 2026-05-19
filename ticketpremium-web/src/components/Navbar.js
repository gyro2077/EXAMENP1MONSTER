'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import styles from './navbar.module.css';

export default function Navbar({ username, logoutAction }) {
  const router = useRouter();
  const [menuOpen, setMenuOpen] = useState(false);

  async function handleLogout() {
    await logoutAction();
    router.push('/login');
  }

  return (
    <nav className={styles.navbar}>
      <div className={styles.inner}>
        <Link href="/dashboard/partidos" className={styles.brand}>
          <span className={styles.brandIcon}>⚽</span>
          <span className={styles.brandText}>
            Ticket<span className="text-gradient">Premium</span>
          </span>
        </Link>

        <div className={styles.links}>
          <Link href="/dashboard/partidos" className={styles.link}>
            Partidos
          </Link>
          <Link href="/dashboard/reportes" className={styles.link}>
            Reportes
          </Link>
          <Link href="/dashboard/evaluador" className={styles.link + ' ' + styles.evalLink}>
            Evaluador
          </Link>
        </div>

        <div className={styles.user}>
          <span className={styles.userName}>{username}</span>
          <button onClick={handleLogout} className={styles.logoutBtn}>
            Salir
          </button>
        </div>

        <button className={styles.menuBtn} onClick={() => setMenuOpen(!menuOpen)}>
          <span />
          <span />
          <span />
        </button>
      </div>

      {menuOpen && (
        <div className={styles.mobileMenu}>
          <Link href="/dashboard/partidos" onClick={() => setMenuOpen(false)}>Partidos</Link>
          <Link href="/dashboard/reportes" onClick={() => setMenuOpen(false)}>Reportes</Link>
          <Link href="/dashboard/evaluador" onClick={() => setMenuOpen(false)}>Evaluador</Link>
          <button onClick={() => { handleLogout(); setMenuOpen(false); }}>Salir</button>
        </div>
      )}
    </nav>
  );
}
