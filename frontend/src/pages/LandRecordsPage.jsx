import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Pencil } from 'lucide-react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

export default function LandRecordsPage() {
  const user = useAuthStore((state) => state.user);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    landApi.getLandRecords()
      .then((data) => {
        const records = Array.isArray(data) ? data : data.content || [];
        setRows(records);
      })
      .catch(() => setError('Could not load land records.'))
      .finally(() => setLoading(false));
  }, []);

  const canEdit = [ROLES.ADMIN, ROLES.DATA_ENTRY].includes(user?.role);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">Land Records</h2>
        {canEdit && (
          <Link to="/lands/new" className="btn-primary">+ Add Land Record</Link>
        )}
      </div>

      {user?.role === ROLES.CITIZEN && (
        <Alert type="info" message="Showing land records mapped to your account." />
      )}
      <Alert message={error} />

      {loading ? (
        <p className="text-slate-500">Loading...</p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">Survey No.</th>
                <th className="px-4 py-3">Owner</th>
                <th className="px-4 py-3">District</th>
                <th className="px-4 py-3">Village</th>
                <th className="px-4 py-3">Area (Acres)</th>
                {canEdit && <th className="px-4 py-3">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {rows.map((row) => (
                <tr key={row.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-brand-700">{row.surveyNumber || row.id}</td>
                  <td className="px-4 py-3">{row.ownerName || row.owner || '—'}</td>
                  <td className="px-4 py-3">{row.district || '—'}</td>
                  <td className="px-4 py-3">{row.village}</td>
                  <td className="px-4 py-3">{row.areaInAcres ?? row.area}</td>
                  {canEdit && (
                    <td className="px-4 py-3">
                      <Link
                        to={`/lands/${row.id}/edit`}
                        className="inline-flex items-center gap-1 rounded px-2 py-1 text-xs text-slate-500 hover:bg-slate-100 hover:text-slate-800"
                      >
                        <Pencil size={13} /> Edit
                      </Link>
                    </td>
                  )}
                </tr>
              ))}
              {rows.length === 0 && (
                <tr>
                  <td colSpan={canEdit ? 6 : 5} className="px-4 py-8 text-center text-slate-400">
                    No land records found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
