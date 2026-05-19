import './globals.css';

export const metadata = {
  title: 'TicketPremium - Boletos de Futbol',
  description: 'Sistema premium de compra de boletos para partidos de futbol',
};

export default function RootLayout({ children }) {
  return (
    <html lang="es" data-scroll-behavior="smooth">
      <body>{children}</body>
    </html>
  );
}
