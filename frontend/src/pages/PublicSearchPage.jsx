import { useState } from 'react';
import { Search, MapPin, AlertTriangle, FileText } from 'lucide-react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const LAND_TYPE_COLORS = {
  PRIVATE: 'bg-emerald-100 text-emerald-700',
  GOVERNMENT: 'bg-blue-100 text-blue-700',
  FOREST: 'bg-green-100 text-green-800',
  ASSIGNED: 'bg-amber-100 text-amber-700',
  INAM: 'bg-violet-100 text-violet-700',
  WAQF: 'bg-orange-100 text-orange-700',
  NALA_CONVERTED: 'bg-cyan-100 text-cyan-700',
};

export default function PublicSearchPage() {
  const [form, setForm] = useState({ district: '', village: '', surveyNumber: '' });
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const set = (k, v) => setForm((p) => ({ ...p, [k]: v }));

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await landApi.publicSearch(form);
      setResults(data);
    } catch {
      setError('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h2 className="page-title">Public Property Search</h2>
          <p className="mt-1 text-sm text-slate-400">Search land records by location — no login required</p>
        </div>
      </div>

      <form className="card grid gap-4 md:grid-cols-3" onSubmit={handleSearch}>
        <div>
          <label className="mb-1 block text-sm font-medium">District</label>
          <input className="input" placeholder="e.g. Rangareddy" value={form.district} onChange={(e) => set('district', e.target.value)} />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Village / Mandal</label>
          <input className="input" placeholder="e.g. Shamshabad" value={form.village} onChange={(e) => set('village', e.target.value)} />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Survey Number</label>
          <input className="input" placeholder="e.g. 123/A" value={form.surveyNumber} onChange={(e) => set('surveyNumber', e.target.value)} />
        </div>
        <div className="md:col-span-3 flex justify-end">
          <button className="btn-primary" disabled={loading || (!form.district && !form.village && !form.surveyNumber)}>
            <Search size={15} /> {loading ? 'Searching…' : 'Search'}
          </button>
        </div>
      </form>

      <Alert message={error} />

      {results !== null && (
        <>
          <p className="text-sm text-slate-500">{results.length} record(s) found</p>
          {results.length > 0 ? (
            <div className="table-container">
              <table className="w-full text-sm">
                <thead>
                  <tr className="table-header">
                    <th className="table-cell">Survey No.</th>
                    <th className="table-cell">Owner</th>
                    <th className="table-cell">District</th>
                    <th className="table-cell">Village</th>
                    <th className="table-cell">Area (Acres)</th>
                    <th className="table-cell">Land Type</th>
                    <th className="table-cell">Passbook</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((row) => (
                    <tr key={row.id} className="table-row">
                      <td className="table-cell font-semibold text-brand-600">{row.surveyNumber}</td>
                      <td className="table-cell font-medium">{row.ownerName}</td>
                      <td className="table-cell text-slate-500">{row.district}</td>
                      <td className="table-cell">
                        <span className="inline-flex items-center gap-1 text-slate-600">
                          <MapPin size={12} className="text-slate-400" /> {row.village}
                        </span>
                      </td>
                      <td className="table-cell text-slate-600">{row.areaInAcres}</td>
                      <td className="table-cell">
                        <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${LAND_TYPE_COLORS[row.landType] || 'bg-slate-100 text-slate-600'}`}>
                          {row.prohibited && <AlertTriangle size={10} />}
                          {row.landType || 'PRIVATE'}
                        </span>
                      </td>
                      <td className="table-cell text-xs text-slate-500">{row.passbookNumber || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">
              <Search size={32} className="opacity-30" />
              <p className="text-sm">No land records found for these criteria</p>
            </div>
          )}
        </>
      )}
    </div>
  );
}
