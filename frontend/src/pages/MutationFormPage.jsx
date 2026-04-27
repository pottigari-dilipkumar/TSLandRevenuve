import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const MUTATION_TYPES = ['SALE', 'SUCCESSION', 'GIFT', 'COURT_ORDER', 'PARTITION', 'NALA_CONVERSION'];

const initial = {
  landRecordId: '',
  registrationRef: '',
  mutationType: 'SALE',
  previousOwnerName: '',
  previousOwnerAadhaar: '',
  newOwnerName: '',
  newOwnerAadhaar: '',
  newOwnerMobile: '',
  newOwnerEmail: '',
  newOwnerAddress: '',
  relationToDeceased: '',
  dateOfDeath: '',
  remarks: '',
};

export default function MutationFormPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initial);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const set = (k, v) => setForm((p) => ({ ...p, [k]: v }));
  const isSuccession = form.mutationType === 'SUCCESSION';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const payload = {
        ...form,
        landRecordId: Number(form.landRecordId),
        dateOfDeath: isSuccession && form.dateOfDeath ? form.dateOfDeath : null,
        registrationRef: form.registrationRef.trim() || null,
        passbookNumber: undefined,
      };
      const res = await landApi.applyMutation(payload);
      navigate(`/mutations/${res.mutationRef}`);
    } catch (err) {
      const ve = err?.response?.data?.validationErrors;
      setError(ve ? Object.values(ve).join(' | ') : err?.response?.data?.message || 'Failed to submit mutation.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="page-title">Apply for Mutation</h2>
        <button className="btn-secondary text-sm" onClick={() => navigate('/mutations')}>← Back</button>
      </div>

      <Alert message={error} />

      <form className="space-y-6" onSubmit={handleSubmit}>
        {/* Mutation details */}
        <div className="card space-y-4">
          <h3 className="section-label">Mutation Details</h3>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium">Land Record ID</label>
              <input className="input" required type="number" min="1" value={form.landRecordId}
                onChange={(e) => set('landRecordId', e.target.value)} placeholder="Enter land record ID" />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Mutation Type</label>
              <select className="input" value={form.mutationType} onChange={(e) => set('mutationType', e.target.value)}>
                {MUTATION_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Registration Ref (optional)</label>
              <input className="input" value={form.registrationRef}
                onChange={(e) => set('registrationRef', e.target.value)} placeholder="REG-XXXXXX" />
            </div>
          </div>
        </div>

        {/* Previous owner */}
        <div className="card space-y-4">
          <h3 className="section-label">Previous Owner (Seller / Deceased)</h3>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium">Name</label>
              <input className="input" required value={form.previousOwnerName}
                onChange={(e) => set('previousOwnerName', e.target.value)} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Aadhaar Number</label>
              <input className="input" maxLength={12} value={form.previousOwnerAadhaar}
                onChange={(e) => set('previousOwnerAadhaar', e.target.value)} />
            </div>
          </div>
        </div>

        {/* New owner */}
        <div className="card space-y-4">
          <h3 className="section-label">New Owner (Buyer / Heir)</h3>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium">Name</label>
              <input className="input" required value={form.newOwnerName}
                onChange={(e) => set('newOwnerName', e.target.value)} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Aadhaar Number</label>
              <input className="input" required maxLength={12} value={form.newOwnerAadhaar}
                onChange={(e) => set('newOwnerAadhaar', e.target.value)} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Mobile</label>
              <input className="input" type="tel" value={form.newOwnerMobile}
                onChange={(e) => set('newOwnerMobile', e.target.value)} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Email</label>
              <input className="input" type="email" value={form.newOwnerEmail}
                onChange={(e) => set('newOwnerEmail', e.target.value)} />
            </div>
            <div className="md:col-span-2">
              <label className="mb-1 block text-sm font-medium">Address</label>
              <textarea className="input resize-none" rows={2} value={form.newOwnerAddress}
                onChange={(e) => set('newOwnerAddress', e.target.value)} />
            </div>
          </div>
        </div>

        {/* Succession-specific */}
        {isSuccession && (
          <div className="card space-y-4 border border-amber-200 bg-amber-50/50">
            <h3 className="section-label text-amber-700">Succession Details</h3>
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <label className="mb-1 block text-sm font-medium">Relation to Deceased</label>
                <input className="input" placeholder="son / daughter / spouse / etc." value={form.relationToDeceased}
                  onChange={(e) => set('relationToDeceased', e.target.value)} />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium">Date of Death</label>
                <input className="input" type="date" value={form.dateOfDeath}
                  onChange={(e) => set('dateOfDeath', e.target.value)} />
              </div>
            </div>
          </div>
        )}

        {/* Remarks */}
        <div className="card">
          <label className="mb-1 block text-sm font-medium">Remarks</label>
          <textarea className="input resize-none" rows={2} value={form.remarks}
            onChange={(e) => set('remarks', e.target.value)} placeholder="Optional notes" />
        </div>

        <div className="flex justify-end gap-3">
          <button type="button" className="btn-secondary" onClick={() => navigate('/mutations')}>Cancel</button>
          <button className="btn-primary" disabled={loading}>
            {loading ? 'Submitting…' : 'Submit Mutation Application'}
          </button>
        </div>
      </form>
    </div>
  );
}
