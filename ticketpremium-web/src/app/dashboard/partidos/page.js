import { getPartidosAction } from '@/actions/partidos';
import PartidosClient from './PartidosClient';

export const dynamic = 'force-dynamic';

export default async function PartidosPage() {
  const result = await getPartidosAction();
  return <PartidosClient partidos={result.data || []} />;
}
