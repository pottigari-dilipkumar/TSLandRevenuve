import { useEffect, useState } from 'react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const fallback = [
  { district: 'North Zone', demand: '₹ 2.1 Cr', collected: '₹ 1.8 Cr', arrears: '₹ 0.3 Cr' },
  { district: 'South Zone', demand: '₹ 1.6 Cr', collected: '₹ 1.1 Cr', arrears: '₹ 0.5 Cr' },
];

export default function RevenueDetailsPage() {
  const [rows, setRows] = useState(fallback);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const data = await landApi.getRevenueDetails();
        const list = Array.isArray(data) ? data : data.items;
        setRows(list?.length ? list : fallback);
      } catch {
        setError('Unable to fetch live revenue data. Using sample figures.');
      }
    };

    load();
  }, []);

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold">Revenue Details</h2>
      <Alert type="info" message={error} />
      <div className="grid gap-4 md:grid-cols-2">
        {rows.map((row) => (
          <div className="card" key={row.district}>
            <h3 className="text-lg font-semibold">{row.district}</h3>
            <div className="mt-3 space-y-1 text-sm text-slate-600">
              <p><span className="font-medium">Demand:</span> {row.demand}</p>
              <p><span className="font-medium">Collected:</span> {row.collected}</p>
              <p><span className="font-medium">Arrears:</span> {row.arrears}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
