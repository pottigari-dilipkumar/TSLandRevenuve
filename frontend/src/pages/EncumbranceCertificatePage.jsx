import { useState } from 'react';
import { Search, FileCheck, ShieldCheck, GitBranch, AlertCircle, Download } from 'lucide-react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

function Section({ title, icon: Icon, count, children }) {
  return (
    <div className="card space-y-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Icon size={16} className="text-brand-500" />
          <h3 className="font-semibold text-slate-700">{title}</h3>
        </div>
        <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-600">{count}</span>
      </div>
      {children}
    </div>
  );
}

export default function EncumbranceCertificatePage() {
  const [form, setForm] = useState({ district: '', village: '', surveyNumber: '' });
  const [ec, setEc] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const set = (k, v) => setForm((p) => ({ ...p, [k]: v }));

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!form.district || !form.village || !form.surveyNumber) {
      setError('District, Village and Survey Number are all required for EC.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const data = await landApi.getEC(form);
      setEc(data);
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not generate EC. Verify the property details.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div>
          <h2 className="page-title">Encumbrance Certificate (EC)</h2>
          <p className="mt-1 text-sm text-slate-400">View all registered transactions and mutations on a property</p>
        </div>
      </div>

      <form className="card grid gap-4 md:grid-cols-3" onSubmit={handleSearch}>
        <div>
          <label className="mb-1 block text-sm font-medium">District <span className="text-red-500">*</span></label>
          <input className="input" required placeholder="e.g. Rangareddy" value={form.district} onChange={(e) => set('district', e.target.value)} />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Village <span className="text-red-500">*</span></label>
          <input className="input" required placeholder="e.g. Shamshabad" value={form.village} onChange={(e) => set('village', e.target.value)} />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Survey Number <span className="text-red-500">*</span></label>
          <input className="input" required placeholder="e.g. 123/A" value={form.surveyNumber} onChange={(e) => set('surveyNumber', e.target.value)} />
        </div>
        <div className="md:col-span-3 flex justify-end">
          <button className="btn-primary" disabled={loading}>
            <Search size={15} /> {loading ? 'Generating EC…' : 'Generate EC'}
          </button>
        </div>
      </form>

      <Alert message={error} />

      {ec && (
        <div className="space-y-6">
          {/* EC Header */}
          <div className="card border border-brand-200 bg-brand-50/40">
            <div className="flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <FileCheck size={18} className="text-brand-600" />
                  <h3 className="font-bold text-slate-800">Encumbrance Certificate</h3>
                </div>
                <p className="text-sm text-slate-600">
                  <span className="font-medium">{ec.district}</span> › {ec.village} › Survey No. <span className="font-mono font-medium">{ec.surveyNumber}</span>
                </p>
                <p className="mt-1 text-xs text-slate-400">Generated: {new Date(ec.generatedAt).toLocaleString()}</p>
              </div>
              <div className="text-right">
                <p className="text-xs text-slate-400">Total Encumbrances</p>
                <p className={`text-2xl font-bold ${ec.encumbranceCount > 0 ? 'text-amber-600' : 'text-emerald-600'}`}>
                  {ec.encumbranceCount}
                </p>
                <p className="text-xs text-slate-400">{ec.encumbranceCount === 0 ? 'Clear title' : 'transactions found'}</p>
              </div>
            </div>
          </div>

          {/* Land records */}
          {ec.landRecords?.length > 0 && (
            <Section title="Land Records" icon={ShieldCheck} count={ec.landRecords.length}>
              {ec.landRecords.map((r) => (
                <div key={r.id} className="grid grid-cols-2 gap-3 rounded-xl bg-slate-50 p-3 text-sm md:grid-cols-4">
                  <div><p className="text-xs text-slate-400">Survey No.</p><p className="font-semibold">{r.surveyNumber}</p></div>
                  <div><p className="text-xs text-slate-400">Owner</p><p>{r.ownerName}</p></div>
                  <div><p className="text-xs text-slate-400">Area</p><p>{r.areaInAcres} acres</p></div>
                  <div><p className="text-xs text-slate-400">Land Type</p><p>{r.landType}</p></div>
                </div>
              ))}
            </Section>
          )}

          {/* Registrations */}
          <Section title="Registered Transactions" icon={FileCheck} count={ec.registrations?.length || 0}>
            {ec.registrations?.length === 0 ? (
              <p className="text-sm text-slate-400">No approved registrations found.</p>
            ) : (
              ec.registrations.map((r, i) => (
                <div key={i} className="rounded-xl border border-slate-100 p-3 text-sm space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs text-brand-600">{r.registrationRef}</span>
                    <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs text-emerald-700">{r.status}</span>
                  </div>
                  <p><span className="text-slate-400">Seller:</span> {r.sellerName} → <span className="text-slate-400">Buyer:</span> {r.buyerName}</p>
                  {r.considerationAmount && <p className="text-slate-500">Consideration: ₹{Number(r.considerationAmount).toLocaleString('en-IN')}</p>}
                  <p className="text-xs text-slate-400">{r.createdAt ? new Date(r.createdAt).toLocaleDateString() : ''}</p>
                  {r.blockchainTxHash && (
                    <p className="text-xs font-mono text-teal-600">⛓ {r.blockchainTxHash.slice(0, 20)}…</p>
                  )}
                </div>
              ))
            )}
          </Section>

          {/* Mutations */}
          <Section title="Mutation History" icon={GitBranch} count={ec.mutations?.length || 0}>
            {ec.mutations?.length === 0 ? (
              <p className="text-sm text-slate-400">No mutation applications found.</p>
            ) : (
              ec.mutations.map((m, i) => (
                <div key={i} className="rounded-xl border border-slate-100 p-3 text-sm space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs text-brand-600">{m.mutationRef}</span>
                    <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">{m.mutationType}</span>
                  </div>
                  <p><span className="text-slate-400">From:</span> {m.previousOwnerName} → <span className="text-slate-400">To:</span> {m.newOwnerName}</p>
                  <p className="text-xs text-slate-400">
                    Applied: {m.appliedAt ? new Date(m.appliedAt).toLocaleDateString() : '—'}
                    {m.decidedAt && ` · Decided: ${new Date(m.decidedAt).toLocaleDateString()}`}
                  </p>
                </div>
              ))
            )}
          </Section>

          {ec.encumbranceCount === 0 && (
            <div className="card border border-emerald-200 bg-emerald-50 flex items-center gap-3">
              <ShieldCheck size={20} className="text-emerald-600 flex-shrink-0" />
              <p className="text-sm text-emerald-700 font-medium">
                This property has a clear title — no encumbrances or pending mutations found.
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
