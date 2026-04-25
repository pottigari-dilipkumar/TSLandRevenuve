import { useEffect, useState } from 'react';
import { marketValueApi } from '../api/citizenApi';
import Alert from '../components/Alert';

export default function MarketValuesPage() {
  const [rates, setRates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    marketValueApi.getAll()
      .then(setRates)
      .catch(() => setError('Failed to load market values'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = rates.filter(
    (r) =>
      r.district.toLowerCase().includes(search.toLowerCase()) ||
      r.village.toLowerCase().includes(search.toLowerCase())
  );

  const districts = [...new Set(rates.map((r) => r.district))].sort();

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">Market Value Rates</h1>
        <input
          className="input w-64"
          placeholder="Search district or village..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>
      <Alert message={error} />
      {loading ? (
        <p className="text-slate-500">Loading...</p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">District</th>
                <th className="px-4 py-3">Village / Area</th>
                <th className="px-4 py-3">Rate Per Acre</th>
                <th className="px-4 py-3">Effective From</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((r) => (
                <tr key={r.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium">{r.district}</td>
                  <td className="px-4 py-3">{r.village}</td>
                  <td className="px-4 py-3 font-medium text-green-700">
                    ₹{Number(r.ratePerAcre).toLocaleString('en-IN')}
                  </td>
                  <td className="px-4 py-3 text-slate-500">{r.effectiveFrom}</td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-slate-400">No results found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
