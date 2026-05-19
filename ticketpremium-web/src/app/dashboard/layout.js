import { redirect } from 'next/navigation';
import { getSession } from '@/actions/auth';
import { logoutAction } from '@/actions/auth';
import Navbar from '@/components/Navbar';

export default async function DashboardLayout({ children }) {
  const session = await getSession();

  if (!session.isAuthenticated) {
    redirect('/login');
  }

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-primary)' }}>
      <Navbar username={session.username} logoutAction={logoutAction} />
      <main style={{ paddingTop: '80px', paddingBottom: '32px', paddingLeft: '16px', paddingRight: '16px', maxWidth: '1280px', marginLeft: 'auto', marginRight: 'auto' }}>
        {children}
      </main>
    </div>
  );
}
