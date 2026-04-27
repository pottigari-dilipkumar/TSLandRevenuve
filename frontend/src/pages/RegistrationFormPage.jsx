import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle, ChevronRight, MapPin } from 'lucide-react';
import Alert from '../components/Alert';
import PolygonMapPicker from '../components/PolygonMapPicker';
import { registrationApi } from '../api/registrationApi';
import { documentApi, marketValueApi } from '../api/citizenApi';

const STEPS = ['Property', 'Seller', 'Buyer', 'Witnesses', 'Documents', 'Review'];

const DOCUMENT_TYPES = [
  'TITLE_DEED',
  'ENCUMBRANCE_CERTIFICATE',
  'ID_PROOF',
  'PHOTOGRAPH',
  'POWER_OF_ATTORNEY',
  'SALE_AGREEMENT',
  'TAX_RECEIPT',
  'OTHER',
];

const emptyParty = { name: '', aadhaarNumber: '', mobile: '', email: '', address: '' };
const emptyWitness = { name: '', aadhaarNumber: '', mobile: '', address: '' };

// ── Defined outside RegistrationFormPage so React never remounts it ──────────
function PartyForm({ label, data, onChange }) {
  return (
    <div className="space-y-4">
      <h3 className="font-semibold text-slate-800">{label} Details</h3>

      <div>
        <label className="mb-1 block text-sm font-medium">Full Name *</label>
        <input
          className="input"
          type="text"
          required
          value={data.name}
          onChange={(e) => onChange({ ...data, name: e.target.value })}
        />
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium">Aadhaar Number *</label>
        <input
          className="input"
          type="text"
          maxLength={12}
          placeholder="12-digit Aadhaar"
          required
          value={data.aadhaarNumber}
          onChange={(e) => onChange({ ...data, aadhaarNumber: e.target.value.replace(/\D/g, '') })}
        />
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium">Mobile Number *</label>
        <input
          className="input"
          type="tel"
          maxLength={10}
          placeholder="10-digit mobile"
          required
          value={data.mobile}
          onChange={(e) => onChange({ ...data, mobile: e.target.value.replace(/\D/g, '') })}
        />
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium">Email (optional)</label>
        <input
          className="input"
          type="email"
          value={data.email}
          onChange={(e) => onChange({ ...data, email: e.target.value })}
        />
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium">Address *</label>
        <textarea
          className="input"
          rows={3}
          required
          value={data.address}
          onChange={(e) => onChange({ ...data, address: e.target.value })}
        />
      </div>
    </div>
  );
}
// ─────────────────────────────────────────────────────────────────────────────

export default function RegistrationFormPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [saved, setSaved] = useState(null);

  const [property, setProperty] = useState({
    propertyDistrict: '',
    propertyVillage: '',
    propertySurveyNumber: '',
    propertyAreaInAcres: '',
    considerationAmount: '',
    notes: '',
  });
  // Polygon boundary + PLUS code drawn on map
  // Shape: { geometry: string, plusCode: string, centroid: {lat, lng} } | null
  const [polygon, setPolygon] = useState(null);
  const [marketInfo, setMarketInfo] = useState(null);
  const [seller, setSeller] = useState(emptyParty);
  const [buyer, setBuyer] = useState(emptyParty);
  const [witnesses, setWitnesses] = useState([{ ...emptyWitness }]);
  const [pendingDocs, setPendingDocs] = useState([]);

  const lookupMarketValue = async () => {
    if (!property.propertyDistrict || !property.propertyVillage) return;
    try {
      const mv = await marketValueApi.lookup(property.propertyDistrict, property.propertyVillage);
      setMarketInfo(mv);
    } catch {
      setMarketInfo(null);
    }
  };

  const updateWitness = (idx, field, value) => {
    setWitnesses((prev) => prev.map((w, i) => (i === idx ? { ...w, [field]: value } : w)));
  };

  const addWitness = () => {
    if (witnesses.length < 3) setWitnesses((prev) => [...prev, { ...emptyWitness }]);
  };

  const removeWitness = (idx) => {
    setWitnesses((prev) => prev.filter((_, i) => i !== idx));
  };

  const addPendingDoc = () => {
    setPendingDocs((prev) => [...prev, { file: null, documentType: 'OTHER', description: '' }]);
  };

  const updatePendingDoc = (idx, field, value) => {
    setPendingDocs((prev) => prev.map((d, i) => (i === idx ? { ...d, [field]: value } : d)));
  };

  const removePendingDoc = (idx) => {
    setPendingDocs((prev) => prev.filter((_, i) => i !== idx));
  };

  const isPartyValid = (p) => p.name && p.aadhaarNumber.length === 12 && p.mobile.length === 10 && p.address;

  const handleCreate = async () => {
    setError('');
    setLoading(true);
    try {
      const payload = {
        ...property,
        propertyAreaInAcres: parseFloat(property.propertyAreaInAcres),
        considerationAmount: parseFloat(property.considerationAmount),
        seller,
        buyer,
        witnesses: witnesses.filter((w) => w.name && w.aadhaarNumber),
        propertyGeometry:  polygon?.geometry  ?? null,
        propertyPlusCode:  polygon?.plusCode  ?? null,
        propertyLatitude:  polygon?.centroid?.lat ?? null,
        propertyLongitude: polygon?.centroid?.lng ?? null,
      };
      const reg = await registrationApi.createDraft(payload);
      setSaved(reg);

      for (const doc of pendingDocs) {
        if (doc.file) {
          await documentApi.upload(reg.registrationRef, doc.file, doc.documentType, doc.description);
        }
      }
      setPendingDocs([]);
      setStep(5);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to create registration');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitForApproval = async () => {
    setError('');
    setLoading(true);
    try {
      await registrationApi.submit(saved.registrationRef);
      navigate('/registrations');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to submit');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-6 text-xl font-bold">New Land Registration</h1>

      {/* Step indicator */}
      <div className="mb-8 flex items-center gap-1">
        {STEPS.map((s, i) => (
          <div key={s} className="flex items-center">
            <div
              className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold
                ${i < step ? 'bg-green-500 text-white' : i === step ? 'bg-brand-600 text-white' : 'bg-slate-200 text-slate-500'}`}
            >
              {i < step ? <CheckCircle size={14} /> : i + 1}
            </div>
            {i < STEPS.length - 1 && (
              <div className={`mx-1 h-0.5 w-6 ${i < step ? 'bg-green-500' : 'bg-slate-200'}`} />
            )}
          </div>
        ))}
        <span className="ml-2 text-sm font-medium text-slate-600">{STEPS[step]}</span>
      </div>

      <Alert message={error} />

      {/* Step 0: Property */}
      {step === 0 && (
        <div className="card space-y-4">
          <h3 className="font-semibold">Property Details</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium">District *</label>
              <input
                className="input"
                required
                value={property.propertyDistrict}
                onChange={(e) => setProperty((p) => ({ ...p, propertyDistrict: e.target.value }))}
                onBlur={lookupMarketValue}
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Village / Area *</label>
              <input
                className="input"
                required
                value={property.propertyVillage}
                onChange={(e) => setProperty((p) => ({ ...p, propertyVillage: e.target.value }))}
                onBlur={lookupMarketValue}
              />
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Survey Number *</label>
            <input
              className="input"
              required
              value={property.propertySurveyNumber}
              onChange={(e) => setProperty((p) => ({ ...p, propertySurveyNumber: e.target.value }))}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium">Area (Acres) *</label>
              <input
                className="input"
                type="number"
                step="0.0001"
                min="0.0001"
                required
                value={property.propertyAreaInAcres}
                onChange={(e) => setProperty((p) => ({ ...p, propertyAreaInAcres: e.target.value }))}
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Consideration Amount (₹) *</label>
              <input
                className="input"
                type="number"
                min="1"
                required
                value={property.considerationAmount}
                onChange={(e) => setProperty((p) => ({ ...p, considerationAmount: e.target.value }))}
              />
            </div>
          </div>

          {marketInfo && (
            <div className="rounded-xl bg-emerald-50 border border-emerald-200 p-3 text-sm">
              <p className="font-semibold text-emerald-800">Market Value Found</p>
              <p className="text-emerald-700">Rate: ₹{Number(marketInfo.ratePerAcre).toLocaleString('en-IN')} / acre</p>
              {property.propertyAreaInAcres && (
                <p className="text-emerald-700">
                  Estimated total: ₹{(Number(marketInfo.ratePerAcre) * Number(property.propertyAreaInAcres)).toLocaleString('en-IN')}
                </p>
              )}
            </div>
          )}

          {/* Land Boundary — polygon drawing on map */}
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <MapPin size={15} className="text-brand-500" />
              <p className="text-sm font-semibold text-slate-700">Land Parcel Boundary</p>
              <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-500">optional but recommended</span>
            </div>
            <p className="text-xs text-slate-400">
              Draw the boundary of the land on the map by clicking corner points, then click "Close Polygon".
              A PLUS Code (Govt. of India location identifier) will be auto-generated.
            </p>
            <PolygonMapPicker value={polygon} onChange={setPolygon} height="360px" />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Notes (optional)</label>
            <textarea
              className="input"
              rows={2}
              value={property.notes}
              onChange={(e) => setProperty((p) => ({ ...p, notes: e.target.value }))}
            />
          </div>

          <button
            className="btn-primary"
            onClick={() => setStep(1)}
            disabled={
              !property.propertyDistrict ||
              !property.propertyVillage ||
              !property.propertySurveyNumber ||
              !property.propertyAreaInAcres ||
              !property.considerationAmount
            }
          >
            Next: Seller Details <ChevronRight size={16} className="inline" />
          </button>
        </div>
      )}

      {/* Step 1: Seller */}
      {step === 1 && (
        <div className="card">
          <PartyForm label="Seller" data={seller} onChange={setSeller} />
          <div className="mt-6 flex gap-3">
            <button className="btn-secondary" onClick={() => setStep(0)}>Back</button>
            <button
              className="btn-primary"
              onClick={() => setStep(2)}
              disabled={!isPartyValid(seller)}
            >
              Next: Buyer Details <ChevronRight size={16} className="inline" />
            </button>
          </div>
        </div>
      )}

      {/* Step 2: Buyer */}
      {step === 2 && (
        <div className="card">
          <PartyForm label="Buyer" data={buyer} onChange={setBuyer} />
          <div className="mt-6 flex gap-3">
            <button className="btn-secondary" onClick={() => setStep(1)}>Back</button>
            <button
              className="btn-primary"
              onClick={() => setStep(3)}
              disabled={!isPartyValid(buyer)}
            >
              Next: Witnesses <ChevronRight size={16} className="inline" />
            </button>
          </div>
        </div>
      )}

      {/* Step 3: Witnesses */}
      {step === 3 && (
        <div className="card space-y-4">
          <h3 className="font-semibold">Witnesses (up to 3)</h3>
          {witnesses.map((w, idx) => (
            <div key={idx} className="rounded-lg border border-slate-200 p-4 space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm font-medium text-slate-700">Witness {idx + 1}</span>
                {witnesses.length > 1 && (
                  <button className="text-xs text-red-500 hover:text-red-700" onClick={() => removeWitness(idx)}>
                    Remove
                  </button>
                )}
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-medium">Name *</label>
                  <input
                    className="input text-sm"
                    required
                    value={w.name}
                    onChange={(e) => updateWitness(idx, 'name', e.target.value)}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium">Aadhaar *</label>
                  <input
                    className="input text-sm"
                    maxLength={12}
                    placeholder="12-digit"
                    value={w.aadhaarNumber}
                    onChange={(e) => updateWitness(idx, 'aadhaarNumber', e.target.value.replace(/\D/g, ''))}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium">Mobile *</label>
                  <input
                    className="input text-sm"
                    maxLength={10}
                    value={w.mobile}
                    onChange={(e) => updateWitness(idx, 'mobile', e.target.value.replace(/\D/g, ''))}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium">Address</label>
                  <input
                    className="input text-sm"
                    value={w.address}
                    onChange={(e) => updateWitness(idx, 'address', e.target.value)}
                  />
                </div>
              </div>
            </div>
          ))}
          {witnesses.length < 3 && (
            <button className="btn-secondary text-sm" onClick={addWitness}>
              + Add Witness
            </button>
          )}
          <div className="flex gap-3 mt-4">
            <button className="btn-secondary" onClick={() => setStep(2)}>Back</button>
            <button className="btn-primary" onClick={() => setStep(4)}>
              Next: Documents <ChevronRight size={16} className="inline" />
            </button>
          </div>
        </div>
      )}

      {/* Step 4: Documents */}
      {step === 4 && (
        <div className="card space-y-4">
          <h3 className="font-semibold">Upload Documents</h3>
          <p className="text-sm text-slate-500">
            Upload previous title deeds, encumbrance certificates, ID proofs, and other relevant documents.
          </p>

          {pendingDocs.map((doc, idx) => (
            <div key={idx} className="rounded-lg border border-slate-200 p-4 space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm font-medium">Document {idx + 1}</span>
                <button className="text-xs text-red-500" onClick={() => removePendingDoc(idx)}>
                  Remove
                </button>
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium">Document Type</label>
                <select
                  className="input text-sm"
                  value={doc.documentType}
                  onChange={(e) => updatePendingDoc(idx, 'documentType', e.target.value)}
                >
                  {DOCUMENT_TYPES.map((t) => (
                    <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium">File</label>
                <input
                  className="input text-sm"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  onChange={(e) => updatePendingDoc(idx, 'file', e.target.files[0])}
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium">Description (optional)</label>
                <input
                  className="input text-sm"
                  value={doc.description}
                  onChange={(e) => updatePendingDoc(idx, 'description', e.target.value)}
                />
              </div>
            </div>
          ))}

          <button className="btn-secondary text-sm" onClick={addPendingDoc}>
            + Add Document
          </button>

          <div className="flex gap-3 mt-4">
            <button className="btn-secondary" onClick={() => setStep(3)}>Back</button>
            <button className="btn-primary" onClick={() => setStep(5)}>
              Next: Review <ChevronRight size={16} className="inline" />
            </button>
          </div>
        </div>
      )}

      {/* Step 5: Review & Submit */}
      {step === 5 && (
        <div className="card space-y-6">
          <h3 className="font-semibold">Review & Submit</h3>

          {saved ? (
            <div className="rounded-lg bg-green-50 border border-green-200 p-4 text-sm text-green-800">
              <p className="font-bold">Draft Created Successfully!</p>
              <p>
                Registration Ref: <span className="font-mono">{saved.registrationRef}</span>
              </p>
              {saved.totalMarketValue && (
                <p>Market Value: ₹{Number(saved.totalMarketValue).toLocaleString('en-IN')}</p>
              )}
              {saved.stampDuty && (
                <p>Stamp Duty (7%): ₹{Number(saved.stampDuty).toLocaleString('en-IN')}</p>
              )}
            </div>
          ) : (
            <div className="space-y-4 text-sm">
              <div className="grid grid-cols-2 gap-4">
                <div className="rounded-lg bg-slate-50 p-3">
                  <p className="font-medium mb-1">Property</p>
                  <p>
                    {property.propertySurveyNumber} — {property.propertyVillage}, {property.propertyDistrict}
                  </p>
                  <p>
                    {property.propertyAreaInAcres} acres · ₹
                    {Number(property.considerationAmount).toLocaleString('en-IN')}
                  </p>
                  {marketInfo && (
                    <p className="text-green-700">
                      Market rate: ₹{Number(marketInfo.ratePerAcre).toLocaleString('en-IN')}/acre
                    </p>
                  )}
                </div>
                <div className="rounded-lg bg-slate-50 p-3">
                  <p className="font-medium mb-1">Parties</p>
                  <p>Seller: {seller.name}</p>
                  <p>Buyer: {buyer.name}</p>
                  <p>Witnesses: {witnesses.filter((w) => w.name).length}</p>
                </div>
              </div>
              <p className="text-slate-500">
                Documents to upload: {pendingDocs.filter((d) => d.file).length}
              </p>
            </div>
          )}

          <Alert message={error} />

          {!saved ? (
            <div className="flex gap-3">
              <button className="btn-secondary" onClick={() => setStep(4)}>Back</button>
              <button className="btn-primary" onClick={handleCreate} disabled={loading}>
                {loading ? 'Creating Draft...' : 'Create Draft'}
              </button>
            </div>
          ) : (
            <div className="flex gap-3">
              <button className="btn-secondary" onClick={() => navigate('/registrations')}>
                Save as Draft
              </button>
              <button className="btn-primary" onClick={handleSubmitForApproval} disabled={loading}>
                {loading ? 'Submitting...' : 'Submit for SRO Approval'}
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
