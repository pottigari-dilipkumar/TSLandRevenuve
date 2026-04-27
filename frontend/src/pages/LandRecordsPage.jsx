import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Pencil, MapPin, Plus, AlertTriangle, History, X, FileText, GitBranch } from 'lucide-react';
import Alert from '../components/Alert';
import PropertyMap from '../components/PropertyMap';
import { landApi } from '../api/landApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

const LAND_TYPE_COLORS = {
  PRIVATE:       'bg-emerald-100 text-emerald-700',
  GOVERNMENT:    'bg-blue-100 text-blue-700',
  FOREST:        'bg-green-100 text-green-800',
  ASSIGNED:      'bg-amber-100 text-amber-700',
  INAM:          'bg-violet-100 text-violet-700',
  WAQF:          'bg-orange-100 text-orange-700',
  NALA_CONVERTED:'bg-cyan-100 text-cyan-700',
};

const HISTORY_TYPE_META = {
  REGISTRATION: { icon: FileText,   cls: 'bg-brand-100 text-brand-700',   label: 'Registration' },
  MUTATION:     { icon: GitBranch,  cls: 'bg-amber-100 text-amber-700',   label: 'Mutation' },
};

const STATUS_DOT = {
  APPROVED: 'bg-emerald-500',
  APPLIED:  'bg-blue-500',
  MANDAL_REVIEW: 'bg-amber-500',
  REJECTED: 'bg-red-500',
};

function HistoryModal({ land, onClose }) {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    landApi.getLandHistory(land.id)
      .then(setHistory)
      .catch(() => setError('Failed to load history.'))
      .finally(() => setLoading(false));
  }, [land.id]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-2xl rounded-2xl bg-white shadow-2xl flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-start justify-between border-b border-slate-100 p-5">
          <div>
            <h2 className="font-bold text-slate-900">Ownership History</h2>
            <p className="text-sm text-slate-500 mt-0.5">
              Survey <span className="font-mono font-medium">{land.surveyNumber}</span> ·
              {land.village}, {land.district}
            </p>
            {land.plusCode && (
              <code className="mt-1 inline-block rounded-md bg-emerald-50 px-2 py-0.5 text-xs font-mono font-bold text-emerald-700 tracking-wider">
                {land.plusCode}
              </code>
            )}
          </div>
          <button onClick={onClose} className="btn-icon ml-4">
            <X size={18} />
          </button>
        </div>

        {/* Current owner highlight */}
        <div className="mx-5 mt-4 rounded-xl border border-brand-200 bg-brand-50/50 px-4 py-3 text-sm">
          <p className="text-xs text-brand-500 font-medium mb-0.5">Current Owner</p>
          <p className="font-bold text-brand-900">{land.ownerName}</p>
        </div>

        {/* Map if polygon present */}
        {land.geometry && (
          <div className="mx-5 mt-3">
            <PropertyMap geometry={land.geometry} lat={null} lng={null} height="180px"
              label={`Survey ${land.surveyNumber}`} />
          </div>
        )}

        {/* History timeline */}
        <div className="flex-1 overflow-y-auto p-5 space-y-3">
          {loading && (
            <div className="flex justify-center py-8">
              <div className="h-5 w-5 animate-spin rounded-full border-2 border-brand-500 border-t-transparent" />
            </div>
          )}
          {error && <p className="text-sm text-red-500">{error}</p>}
          {!loading && history.length === 0 && (
            <div className="empty-state py-10">
              <History size={28} className="opacity-30" />
              <p className="text-sm">No transaction history found for this land record.</p>
              <p className="text-xs text-slate-400">History is populated when registrations are approved.</p>
            </div>
          )}

          {history.map((entry, i) => {
            const meta = HISTORY_TYPE_META[entry.type] || HISTORY_TYPE_META.REGISTRATION;
            const Icon = meta.icon;
            const dotCls = STATUS_DOT[entry.status] || 'bg-slate-400';
            return (
              <div key={i} className="flex gap-3">
                {/* Timeline spine */}
                <div className="flex flex-col items-center">
                  <div className={`mt-1.5 h-3 w-3 rounded-full flex-shrink-0 border-2 border-white shadow ${dotCls}`} />
                  {i < history.length - 1 && <div className="mt-1 w-px flex-1 bg-slate-200" />}
                </div>
                {/* Content */}
                <div className="pb-4 flex-1">
                  <div className="flex flex-wrap items-center gap-2 mb-1">
                    <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${meta.cls}`}>
                      <Icon size={10} /> {meta.label}
                    </span>
                    <span className="font-mono text-xs text-slate-400">{entry.ref}</span>
                    <span className={`ml-auto rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_DOT[entry.status] ? '' : 'bg-slate-100 text-slate-500'}`}>
                      {entry.status}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 text-sm">
                    <span className="text-slate-500">{entry.previousOwner}</span>
                    <span className="text-slate-300">→</span>
                    <span className="font-semibold text-slate-700">{entry.newOwner}</span>
                  </div>
                  {entry.details && <p className="text-xs text-slate-400 mt-0.5">{entry.details}</p>}
                  {entry.date && (
                    <p className="text-xs text-slate-400 mt-0.5">
                      {new Date(entry.date).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </p>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        <div className="border-t border-slate-100 px-5 py-3 flex justify-end">
          <button className="btn-secondary text-sm" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

export default function LandRecordsPage() {
  const user = useAuthStore((state) => state.user);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [historyLand, setHistoryLand] = useState(null);

  useEffect(() => {
    landApi.getLandRecords()
      .then((data) => setRows(Array.isArray(data) ? data : data.content || []))
      .catch(() => setError('Could not load land records.'))
      .finally(() => setLoading(false));
  }, []);

  const canEdit = [ROLES.ADMIN, ROLES.DATA_ENTRY].includes(user?.role);

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h2 className="page-title">Land Records</h2>
          <p className="mt-1 text-sm text-slate-400">{rows.length} records loaded</p>
        </div>
        {canEdit && (
          <Link to="/lands/new" className="btn-primary">
            <Plus size={15} /> Add Land Record
          </Link>
        )}
      </div>

      {user?.role === ROLES.CITIZEN && (
        <Alert type="info" message="Showing land records mapped to your account." />
      )}
      <Alert message={error} />

      {loading ? (
        <div className="flex items-center justify-center py-20 text-slate-400">
          <div className="h-6 w-6 animate-spin rounded-full border-2 border-brand-500 border-t-transparent" />
          <span className="ml-3 text-sm">Loading records…</span>
        </div>
      ) : (
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
                <th className="table-cell">Passbook / PLUS Code</th>
                <th className="table-cell">Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id} className="table-row">
                  <td className="table-cell">
                    <span className="font-semibold text-brand-600">{row.surveyNumber || row.id}</span>
                  </td>
                  <td className="table-cell font-medium text-slate-700">{row.ownerName || '—'}</td>
                  <td className="table-cell text-slate-500">{row.district || '—'}</td>
                  <td className="table-cell">
                    <span className="inline-flex items-center gap-1 text-slate-600">
                      <MapPin size={12} className="text-slate-400" />
                      {row.village}
                    </span>
                  </td>
                  <td className="table-cell text-slate-600">{row.areaInAcres}</td>
                  <td className="table-cell">
                    <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${LAND_TYPE_COLORS[row.landType] || 'bg-slate-100 text-slate-600'}`}>
                      {row.prohibited && <AlertTriangle size={10} />}
                      {row.landType || 'PRIVATE'}
                    </span>
                  </td>
                  <td className="table-cell text-xs">
                    {row.plusCode ? (
                      <code className="rounded bg-emerald-50 px-1.5 py-0.5 font-mono text-emerald-700">{row.plusCode}</code>
                    ) : (
                      <span className="text-slate-400">{row.passbookNumber || '—'}</span>
                    )}
                  </td>
                  <td className="table-cell">
                    <div className="flex items-center gap-1.5">
                      <button
                        onClick={() => setHistoryLand(row)}
                        className="btn-ghost py-1.5 px-2.5 text-xs flex items-center gap-1"
                        title="Show ownership history"
                      >
                        <History size={12} /> History
                      </button>
                      {canEdit && (
                        <Link
                          to={`/lands/${row.id}/edit`}
                          className="btn-ghost py-1.5 px-2.5 text-xs flex items-center gap-1"
                        >
                          <Pencil size={12} /> Edit
                        </Link>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {rows.length === 0 && (
                <tr>
                  <td colSpan={8}>
                    <div className="empty-state">
                      <MapPin size={32} className="opacity-30" />
                      <p className="text-sm">No land records found</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* History modal */}
      {historyLand && (
        <HistoryModal land={historyLand} onClose={() => setHistoryLand(null)} />
      )}
    </div>
  );
}
