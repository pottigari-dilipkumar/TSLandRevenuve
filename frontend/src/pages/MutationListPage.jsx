import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { GitBranch, Plus, Filter } from 'lucide-react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

const STATUS_COLORS = {
  APPLIED: 'bg-blue-100 text-blue-700',
  MANDAL_REVIEW: 'bg-amber-100 text-amber-700',
  APPROVED: 'bg-emerald-100 text-emerald-700',
  REJECTED: 'bg-red-100 text-red-700',
};

const STATUSES = ['', 'APPLIED', 'MANDAL_REVIEW', 'APPROVED', 'REJECTED'];

export default function MutationListPage() {
  const user = useAuthStore((s) => s.user);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const canDecide = [ROLES.ADMIN, ROLES.REVENUE_OFFICER].includes(user?.role);
  const canApply = [ROLES.ADMIN, ROLES.DATA_ENTRY, ROLES.CITIZEN].includes(user?.role);

  useEffect(() => {
    setLoading(true);
    landApi.getMutations({ status: status || undefined, page, size: 20 })
      .then((data) => {
        setRows(data.content || []);
        setTotalPages(data.totalPages || 1);
      })
      .catch(() => setError('Could not load mutations.'))
      .finally(() => setLoading(false));
  }, [status, page]);

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h2 className="page-title">Mutation Applications</h2>
          <p className="mt-1 text-sm text-slate-400">Ownership transfers — SALE, SUCCESSION, GIFT, etc.</p>
        </div>
        {canApply && (
          <Link to="/mutations/new" className="btn-primary">
            <Plus size={15} /> Apply Mutation
          </Link>
        )}
      </div>

      <Alert message={error} />

      <div className="flex items-center gap-3">
        <Filter size={14} className="text-slate-400" />
        <select
          className="input w-48 text-sm"
          value={status}
          onChange={(e) => { setStatus(e.target.value); setPage(0); }}
        >
          {STATUSES.map((s) => (
            <option key={s} value={s}>{s || 'All Statuses'}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-20 text-slate-400">
          <div className="h-6 w-6 animate-spin rounded-full border-2 border-brand-500 border-t-transparent" />
          <span className="ml-3 text-sm">Loading…</span>
        </div>
      ) : (
        <div className="table-container">
          <table className="w-full text-sm">
            <thead>
              <tr className="table-header">
                <th className="table-cell">Ref</th>
                <th className="table-cell">Type</th>
                <th className="table-cell">Land ID</th>
                <th className="table-cell">Previous Owner</th>
                <th className="table-cell">New Owner</th>
                <th className="table-cell">Status</th>
                <th className="table-cell">Applied</th>
                <th className="table-cell">Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id} className="table-row">
                  <td className="table-cell font-mono text-xs text-brand-600">{row.mutationRef}</td>
                  <td className="table-cell">
                    <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                      {row.mutationType}
                    </span>
                  </td>
                  <td className="table-cell text-slate-500">#{row.landRecordId}</td>
                  <td className="table-cell font-medium">{row.previousOwnerName}</td>
                  <td className="table-cell font-medium">{row.newOwnerName}</td>
                  <td className="table-cell">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[row.status] || 'bg-slate-100 text-slate-600'}`}>
                      {row.status}
                    </span>
                  </td>
                  <td className="table-cell text-slate-400 text-xs">
                    {row.appliedAt ? new Date(row.appliedAt).toLocaleDateString() : '—'}
                  </td>
                  <td className="table-cell">
                    <Link to={`/mutations/${row.mutationRef}`} className="btn-ghost py-1.5 px-2.5 text-xs">
                      View
                    </Link>
                  </td>
                </tr>
              ))}
              {rows.length === 0 && (
                <tr>
                  <td colSpan={8}>
                    <div className="empty-state">
                      <GitBranch size={32} className="opacity-30" />
                      <p className="text-sm">No mutation applications found</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button className="btn-secondary text-xs" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
            Prev
          </button>
          <span className="text-sm text-slate-500">Page {page + 1} of {totalPages}</span>
          <button className="btn-secondary text-xs" onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>
            Next
          </button>
        </div>
      )}
    </div>
  );
}
