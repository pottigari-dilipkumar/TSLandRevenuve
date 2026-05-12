// Copyright (c) 2026 DivaTech. All rights reserved.
// Proprietary and confidential. Unauthorized copying or distribution is strictly prohibited.
// Website: https://www.divatech.in | Contact: legal@divatech.in

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2, Home, User, DollarSign } from 'lucide-react';
import { citizenApi } from '../api/citizenApi';
import { registrationApi } from '../api/registrationApi';
import Alert from '../components/Alert';

function Field({ label, required, children }) {
  return (
    <div>
      <label className="mb-1 block text-xs font-semibold text-slate-600">
        {label}{required && <span className="text-red-500 ml-0.5">*</span>}
      </label>
      {children}
    </div>
  );
}

function SectionCard({ icon: Icon, title, children }) {
  return (
    <div className="card space-y-4">
      <h3 className="flex items-center gap-2 font-semibold text-slate-800 text-sm">
        <Icon size={15} className="text-brand-500" /> {title}
      </h3>
      {children}
    </div>
  );
}

export default function SaleRequestFormPage() {
  const navigate = useNavigate();
  const [lands, setLands] = useState([]);
  const [landsLoading, setLandsLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [form, setForm] = useState({
    landRecordId: '',
    considerationAmount: '',
    buyerName: '',
    buyerAadhaar: '',
    buyerMobile: '',
    buyerEmail: '',
    buyerAddress: '',
    notes: '',
  });

  useEffect(() => {
    citizenApi.getMyLands()
      .then(setLands)
      .catch(() => setError('Failed to load your land holdings'))
      .finally(() => setLandsLoading(false));
  }, []);

  const selectedLand = lands.find(l => String(l.id) === String(form.landRecordId));

  const set = (key, val) => setForm(p => ({ ...p, [key]: val }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.landRecordId) { setError('Please select a land parcel'); return; }
    if (!form.buyerAadhaar || form.buyerAadhaar.length < 12) {
      setError('Buyer Aadhaar must be 12 digits'); return;
    }
    setSubmitting(true);
    setError('');
    try {
      const reg = await registrationApi.createSaleRequest({
        landRecordId: Number(form.landRecordId),
        considerationAmount: Number(form.considerationAmount),
        buyerName: form.buyerName,
        buyerAadhaar: form.buyerAadhaar,
        buyerMobile: form.buyerMobile || null,
        buyerEmail: form.buyerEmail || null,
        buyerAddress: form.buyerAddress || null,
        notes: form.notes || null,
      });
      navigate(`/registrations/${reg.registrationRef}`);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to create sale request');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Initiate Land Sale</h1>
          <p className="text-sm text-slate-500 mt-0.5">Create a sale request — the buyer must consent before it reaches the SRO</p>
        </div>
        <button className="btn-secondary text-sm" onClick={() => navigate('/citizen/dashboard')}>
          <ArrowLeft size={14} /> Back
        </button>
      </div>

      <Alert message={error} onDismiss={() => setError('')} />

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Land selection */}
        <SectionCard icon={Home} title="Select Your Land Parcel">
          {landsLoading ? (
            <div className="flex items-center gap-2 text-slate-400 text-sm">
              <Loader2 size={14} className="animate-spin" /> Loading your lands…
            </div>
          ) : lands.length === 0 ? (
            <p className="text-sm text-slate-500">No land records linked to your Aadhaar.</p>
          ) : (
            <div className="space-y-2">
              {lands.map(land => (
                <label
                  key={land.id}
                  className={`flex items-start gap-3 rounded-xl border p-3 cursor-pointer transition-all ${
                    String(form.landRecordId) === String(land.id)
                      ? 'border-brand-400 bg-brand-50'
                      : 'border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="land"
                    value={land.id}
                    checked={String(form.landRecordId) === String(land.id)}
                    onChange={() => set('landRecordId', land.id)}
                    className="mt-0.5 accent-brand-500"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-slate-800 text-sm">{land.surveyNumber}</p>
                    <p className="text-xs text-slate-500">{land.village}, {land.district} · {land.areaInAcres} acres</p>
                    {land.estimatedMarketValue > 0 && (
                      <p className="text-xs text-emerald-600 font-medium mt-0.5">
                        Est. market value: ₹{Number(land.estimatedMarketValue).toLocaleString('en-IN')}
                      </p>
                    )}
                  </div>
                </label>
              ))}
            </div>
          )}

          {selectedLand && (
            <Field label="Agreed Sale Price (₹)" required>
              <input
                type="number"
                className="input"
                placeholder="e.g. 2500000"
                min="1"
                required
                value={form.considerationAmount}
                onChange={e => set('considerationAmount', e.target.value)}
              />
              <p className="text-[11px] text-slate-400 mt-1">
                Stamp duty (7%) will be calculated on the higher of market value or agreed price
              </p>
            </Field>
          )}
        </SectionCard>

        {/* Buyer details */}
        <SectionCard icon={User} title="Buyer Details">
          <p className="text-xs text-slate-500 -mt-2">
            Enter the buyer's details. They must have a registered citizen account with the same Aadhaar number.
          </p>
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <Field label="Full Name" required>
                <input
                  className="input"
                  placeholder="Buyer's full name"
                  required
                  value={form.buyerName}
                  onChange={e => set('buyerName', e.target.value)}
                />
              </Field>
            </div>
            <div className="col-span-2">
              <Field label="Aadhaar Number" required>
                <input
                  className="input font-mono"
                  placeholder="12-digit Aadhaar"
                  maxLength={12}
                  required
                  value={form.buyerAadhaar}
                  onChange={e => set('buyerAadhaar', e.target.value.replace(/\D/g, ''))}
                />
              </Field>
            </div>
            <Field label="Mobile">
              <input
                className="input"
                placeholder="+91 9XXXXXXXXX"
                value={form.buyerMobile}
                onChange={e => set('buyerMobile', e.target.value)}
              />
            </Field>
            <Field label="Email">
              <input
                type="email"
                className="input"
                placeholder="buyer@email.com"
                value={form.buyerEmail}
                onChange={e => set('buyerEmail', e.target.value)}
              />
            </Field>
            <div className="col-span-2">
              <Field label="Address">
                <textarea
                  className="input"
                  rows={2}
                  placeholder="Buyer's address"
                  value={form.buyerAddress}
                  onChange={e => set('buyerAddress', e.target.value)}
                />
              </Field>
            </div>
          </div>
        </SectionCard>

        {/* Notes */}
        <SectionCard icon={DollarSign} title="Additional Notes">
          <Field label="Notes (optional)">
            <textarea
              className="input"
              rows={3}
              placeholder="Any additional terms, conditions, or remarks…"
              value={form.notes}
              onChange={e => set('notes', e.target.value)}
            />
          </Field>
        </SectionCard>

        <div className="flex gap-3 justify-end">
          <button type="button" className="btn-secondary" onClick={() => navigate('/citizen/dashboard')}>
            Cancel
          </button>
          <button
            type="submit"
            className="btn-primary"
            disabled={submitting || !form.landRecordId}
          >
            {submitting && <Loader2 size={14} className="animate-spin" />}
            {submitting ? 'Creating…' : 'Create Sale Request'}
          </button>
        </div>
      </form>
    </div>
  );
}
