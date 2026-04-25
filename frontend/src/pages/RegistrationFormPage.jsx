import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle, ChevronRight } from 'lucide-react';
import Alert from '../components/Alert';
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

export default function RegistrationFormPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [saved, setSaved] = useState(null); // created registration

  const [property, setProperty] = useState({
    propertyDistrict: '',
    propertyVillage: '',
    propertySurveyNumber: '',
    propertyAreaInAcres: '',
    considerationAmount: '',
    notes: '',
  });
  const [marketInfo, setMarketInfo] = useState(null);
  const [seller, setSeller] = useState(emptyParty);
  const [buyer, setBuyer] = useState(emptyParty);
  const [witnesses, setWitnesses] = useState([{ ...emptyWitness }]);
  const [documents, setDocuments] = useState([]);
  const [pendingDocs, setPendingDocs] = useState([]); // { file, documentType, description }

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
    setWitnesses((prev) => prev.map((w, i) => i === idx ? { ...w, [field]: value } : w));
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
    setPendingDocs((prev) => prev.map((d, i) => i === idx ? { ...d, [field]: value } : d));
  };

  const removePendingDoc = (idx) => {
    setPendingDocs((prev) => prev.filter((_, i) => i !== idx));
  };

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
      };
      const reg = await registrationApi.createDraft(payload);
      setSaved(reg);

      // Upload pending documents
      for (const doc of pendingDocs) {
        if (doc.file) {
          const uploaded = await documentApi.upload(reg.registrationRef, doc.file, doc.documentType, doc.description);
          setDocuments((prev) => [...prev, uploaded]);
        }
      }
      setPendingDocs([]);
      setStep(5); // go to review
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

  const PartyForm = ({ label, data, onChange }) => (
    <div className="space-y-4">
      <h3 className="font-semibold text-slate-800">{label} Details</h3>
      {[
        { field: 'name', label: 'Full Name', type: 'text', required: true },
        { field: 'aadhaarNumber', label: 'Aadhaar Number', type: 'text', maxLength: 12, placeholder: '12-digit', required: true },
        { field: 'mobile', label: 'Mobile Number', type: 'tel', maxLength: 10, placeholder: '10-digit', required: true },
        { field: 'email', label: 'Email (optional)', type: 'email' },
        { field: 'address', label: 'Address', type: 'textarea', required: true },
      ].map(({ field, label, type, maxLength, placeholder, required }) => (
        <div key={field}>
          <label className="mb-1 block text-sm font-medium">{label}</label>
          {type === 'textarea' ? (
            <textarea className="input" rows={2} required={required} value={data[field]}
              onChange={(e) => onChange({ ...data, [field]: e.target.value })} />
          ) : (
            <input className="input" type={type} maxLength={maxLength} placeholder={placeholder}
              required={required} value={data[field]}
              onChange={(e) => {
                const val = (type === 'tel') ? e.target.value.replace(/\D/g, '') : e.target.value;
                onChange({ ...data, [field]: val });
              }} />
          )}
        </div>
      ))}
    </div>
  );

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-6 text-xl font-bold">New Land Registration</h1>

      {/* Step indicator */}
      <div className="mb-8 flex items-center gap-1">
        {STEPS.map((s, i) => (
          <div key={s} className="flex items-center">
            <div className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold
              ${i < step ? 'bg-green-500 text-white' : i === step ? 'bg-brand-600 text-white' : 'bg-slate-200 text-slate-500'}`}>
              {i < step ? <CheckCircle size={14} /> : i + 1}
            </div>
            {i < STEPS.length - 1 && <div className={`mx-1 h-0.5 w-6 ${i < step ? 'bg-green-500' : 'bg-slate-200'}`} />}
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
              <input className="input" required value={property.propertyDistrict}
                onChange={(e) => setProperty((p) => ({ ...p, propertyDistrict: e.target.value }))}
                onBlur={lookupMarketValue} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Village/Area *</label>
              <input className="input" required value={property.propertyVillage}
                onChange={(e) => setProperty((p) => ({ ...p, propertyVillage: e.target.value }))}
                onBlur={lookupMarketValue} />
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Survey Number *</label>
            <input className="input" required value={property.propertySurveyNumber}
              onChange={(e) => setProperty((p) => ({ ...p, propertySurveyNumber: e.target.value }))} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium">Area (Acres) *</label>
              <input className="input" type="number" step="0.0001" min="0.0001" required value={property.propertyAreaInAcres}
                onChange={(e) => setProperty((p) => ({ ...p, propertyAreaInAcres: e.target.value }))} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Consideration Amount (₹) *</label>
              <input className="input" type="number" min="1" required value={property.considerationAmount}
                onChange={(e) => setProperty((p) => ({ ...p, considerationAmount: e.target.value }))} />
            </div>
          </div>

          {marketInfo && (
            <div className="rounded-lg bg-green-50 border border-green-200 p-3 text-sm">
              <p className="font-medium text-green-800">Market Value Found</p>
              <p className="text-green-700">Rate: ₹{Number(marketInfo.ratePerAcre).toLocaleString('en-IN')} / acre</p>
              {property.propertyAreaInAcres && (
                <p className="text-green-700">
                  Estimated: ₹{(Number(marketInfo.ratePerAcre) * Number(property.propertyAreaInAcres)).toLocaleString('en-IN')}
                </p>
              )}
            </div>
          )}

          <div>
            <label className="mb-1 block text-sm font-medium">Notes (optional)</label>
            <textarea className="input" rows={2} value={property.notes}
              onChange={(e) => setProperty((p) => ({ ...p, notes: e.target.value }))} />
          </div>
          <button className="btn-primary" onClick={() => setStep(1)}
            disabled={!property.propertyDistrict || !property.propertyVillage || !property.propertySurveyNumber || !property.propertyAreaInAcres || !property.considerationAmount}>
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
            <button className="btn-primary" onClick={() => setStep(2)}
              disabled={!seller.name || !seller.aadhaarNumber || !seller.mobile || !seller.address}>
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
            <button className="btn-primary" onClick={() => setStep(3)}
              disabled={!buyer.name || !buyer.aadhaarNumber || !buyer.mobile || !buyer.address}>
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
                  <button className="text-xs text-red-500 hover:text-red-700" onClick={() => removeWitness(idx)}>Remove</button>
                )}
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-medium">Name *</label>
                  <input className="input text-sm" required value={w.name} onChange={(e) => updateWitness(idx, 'name', e.target.value)} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium">Aadhaar *</label>
                  <input className="input text-sm" maxLength={12} placeholder="12-digit" value={w.aadhaarNumber}
                    onChange={(e) => updateWitness(idx, 'aadhaarNumber', e.target.value.replace(/\D/g, ''))} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium">Mobile *</label>
                  <input className="input text-sm" maxLength={10} value={w.mobile}
                    onChange={(e) => updateWitness(idx, 'mobile', e.target.value.replace(/\D/g, ''))} />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium">Address</label>
                  <input className="input text-sm" value={w.address} onChange={(e) => updateWitness(idx, 'address', e.target.value)} />
                </div>
              </div>
            </div>
          ))}
          {witnesses.length < 3 && (
            <button className="btn-secondary text-sm" onClick={addWitness}>+ Add Witness</button>
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
          <p className="text-sm text-slate-500">Upload previous title deeds, encumbrance certificates, ID proofs, and other relevant documents.</p>

          {pendingDocs.map((doc, idx) => (
            <div key={idx} className="rounded-lg border border-slate-200 p-4 space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-sm font-medium">Document {idx + 1}</span>
                <button className="text-xs text-red-500" onClick={() => removePendingDoc(idx)}>Remove</button>
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium">Document Type</label>
                <select className="input text-sm" value={doc.documentType}
                  onChange={(e) => updatePendingDoc(idx, 'documentType', e.target.value)}>
                  {DOCUMENT_TYPES.map((t) => (
                    <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium">File</label>
                <input className="input text-sm" type="file" accept=".pdf,.jpg,.jpeg,.png"
                  onChange={(e) => updatePendingDoc(idx, 'file', e.target.files[0])} />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium">Description (optional)</label>
                <input className="input text-sm" value={doc.description}
                  onChange={(e) => updatePendingDoc(idx, 'description', e.target.value)} />
              </div>
            </div>
          ))}

          <button className="btn-secondary text-sm" onClick={addPendingDoc}>+ Add Document</button>

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
              <p>Registration Ref: <span className="font-mono">{saved.registrationRef}</span></p>
              {saved.totalMarketValue && <p>Market Value: ₹{Number(saved.totalMarketValue).toLocaleString('en-IN')}</p>}
              {saved.stampDuty && <p>Stamp Duty (7%): ₹{Number(saved.stampDuty).toLocaleString('en-IN')}</p>}
            </div>
          ) : (
            <div className="space-y-4 text-sm">
              <div className="grid grid-cols-2 gap-4">
                <div className="rounded-lg bg-slate-50 p-3">
                  <p className="font-medium mb-1">Property</p>
                  <p>{property.propertySurveyNumber} — {property.propertyVillage}, {property.propertyDistrict}</p>
                  <p>{property.propertyAreaInAcres} acres · ₹{Number(property.considerationAmount).toLocaleString('en-IN')}</p>
                  {marketInfo && <p className="text-green-700">Market rate: ₹{Number(marketInfo.ratePerAcre).toLocaleString('en-IN')}/acre</p>}
                </div>
                <div className="rounded-lg bg-slate-50 p-3">
                  <p className="font-medium mb-1">Parties</p>
                  <p>Seller: {seller.name} ({seller.aadhaarNumber.slice(-4) ? '****' + seller.aadhaarNumber.slice(-4) : ''})</p>
                  <p>Buyer: {buyer.name} ({buyer.aadhaarNumber ? '****' + buyer.aadhaarNumber.slice(-4) : ''})</p>
                  <p>Witnesses: {witnesses.filter(w => w.name).length}</p>
                </div>
              </div>
              <p className="text-slate-500">Documents to upload: {pendingDocs.filter(d => d.file).length}</p>
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
              <button className="btn-secondary" onClick={() => navigate('/registrations')}>Save as Draft</button>
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
