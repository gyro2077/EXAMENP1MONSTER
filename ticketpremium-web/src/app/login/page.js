import { loginAction } from '@/actions/auth';
import { redirect } from 'next/navigation';
import styles from './login.module.css';

export default async function LoginPage({ searchParams }) {
  const awaitedSearchParams = await searchParams;
  const error = awaitedSearchParams?.error || '';

  return (
    <div className={styles.container}>
      <div className={styles.bgGradient} />
      <div className={styles.card}>
        <div className={styles.logo}>
          <span className={styles.logoIcon}>⚽</span>
          <h1 className={styles.title}>
            Ticket<span className="text-gradient">Premium</span>
          </h1>
        </div>
        <p className={styles.subtitle}>Accede a los mejores partidos de futbol</p>

        <form action={handleLogin} className={styles.form}>
          <div className={styles.field}>
            <label htmlFor="username">Usuario</label>
            <input
              id="username"
              name="username"
              type="text"
              placeholder="Tu nombre de usuario"
              required
              autoComplete="username"
            />
          </div>

          <div className={styles.field}>
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="Tu contraseña"
              required
              autoComplete="current-password"
            />
          </div>

          {error && (
            <div className={styles.error}>
              {decodeURIComponent(error)}
            </div>
          )}

          <button type="submit" className="btn-primary">
            Iniciar Sesion
          </button>
        </form>

        <div className={styles.demo}>
          <p>Demo: <strong>MONSTER</strong> / <strong>MONSTER9</strong></p>
        </div>
      </div>
    </div>
  );
}

async function handleLogin(formData) {
  'use server';
  const result = await loginAction(formData);
  if (result.success) {
    redirect('/dashboard/partidos');
  } else {
    redirect('/login?error=' + encodeURIComponent(result.message));
  }
}
