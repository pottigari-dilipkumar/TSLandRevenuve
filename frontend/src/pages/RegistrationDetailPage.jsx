import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { CheckCircle, XCircle, Upload, MapPin, Link2, ShieldCheck, ArrowLeft } from 'lucide-react';
import { registrationApi } from '../api/registrationApi';
import { documentApi } from '../api/citizenApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';
import Alert from '../components/Alert';
import PropertyMap from '../components/PropertyMap';
import BlockchainTimeline from '../components/BlockchainTimeline';

const DOCUMENT_TYPES = [
  'TITLE_DEED', 'ENCUMBRANCE_CERTIFICATE', 'ID_PROOF', 'PHOTOGRAPH',
  'POWER_OF_ATTORNEY', 'SALE_AGREEMENT', 'TAX_RECEIPT', 'OTHER',
];

const STATUS_META = {
  DRAFT:            { cls: 'bg-slate-100 text-slate-600 border-slate-200',     label: 'Draft' },
  PENDING_APPROVAL: { cls: 'bg-amber-50 text-amber-700 border-amber-200',      label: 'Pending Approval' },
  APPROVED:         { cls: 'bg-emerald-50 text-emerald-700 border-emerald-200', label: 'Approved' },
  REJECTED:         { cls: 'bg-rose-50 text-rose-700 border-rose-200',          label: 'Rejected' },
};

const CHAIN_STATUS = {
  SYNCED:  { cls: 'bg-emerald-50 text-emerald-700 border-emerald-200', label: 'On-chain ✓' },
  FAILED:  { cls: 'bg-rose-50 text-rose-700 border-rose-200',           label: 'Anchor Failed' },
  SKIPPED: { cls: 'bg-slate-50 text-slate-400 border-slate-200',        label: 'Blockchain Disabled' },
};

function Section({ title, icon: Icon, children }) {
  return (
    <div className="card space-y-4">
      <h3 className="flex items-center gap-2 font-semibold text-slate-800">
        {Icon && <Icon size={15} className="text-slate-400" />}
        {title}
      </h3>
      {children}
    </div>
  );
}

function Field({ label, value }) {
  return (
    <div>
      <p className="section-label mb-0.5">{label}</p>
      <p className="text-sm font-medium text-slate-700">{value || '—'}</p>
    </div>
  );
}

function PartyCard({ title, name, aadhaar, mobile, email, address }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 space-y-3">
      <p className="text-xs font-bold uppercase tracking-widest text-slate-400">{title}</p>
      <p className="text-base font-semibold text-slate-800">{name}</p>
      <div className="grid grid-cols-2 gap-3 text-sm">
        <div><p className="section-label mb-0.5">Aadhaar</p><p className="font-mono font-medium">****{(aadhaar || '').slice(-4)}</p></div>
        <div><p className="section-label mb-0.5">Mobile</p><p>{mobile || '—'}</p></div>
        <div className="col-span-2"><p className="section-label mb-0.5">Email</p><p>{email || '—'}</p></div>
        {address && <div className="col-span-2"><p className="section-label mb-0.5">Address</p><p>{address}</p></div>}
      </div>
    </div>
  );
}

export default function RegistrationDetailPage() {
  const { ref } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [reg, setReg] = useState(null);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [eventsLoading, setEventsLoading] = useState(true);
  const [error, setError] = useState('');
  const [uploadDoc, setUploadDoc] = useState({ file: null, documentType: 'OTHER', description: '' });
  const [uploading, setUploading] = useState(false);
  const [activeTab, setActiveTab] = useState('details');

  const canApprove = [ROLES.SRO, ROLES.ADMIN].includes(user?.role);

  const loadReg = async () => {
    try {
      const data = await registrationApi.getByRef(ref);
      setReg(data);
    } catch {
      setError('Registration not found');
    } finally {
      setLoading(false);
    }
  };

  const loadEvents = async () => {
    try {
      const data = await registrationApi.getEvents(ref);
      setEvents(Array.isArray(data) ? data : []);
    } catch {
      setEvents([]);
    } finally {
      setEventsLoading(false);
    }
  };

  useEffect(() => { loadReg(); loadEvents(); }, [ref]);

  const handleApprove = async () => {
    try { await registrationApi.approve(ref); loadReg(); loadEvents(); }
    catch (err) { setError(err?.response?.data?.message || 'Failed to approve'); }
  };

  const handleReject = async () => {
    const reason = prompt('Enter rejection reason:');
    if (!reason) return;
    try { await registrationApi.reject(ref, reason); loadReg(); loadEvents(); }
    catch (err) { setError(err?.response?.data?.message || 'Failed to reject'); }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!uploadDoc.file) return;
    setUploading(true);
    try {
      await documentApi.upload(ref, uploadDoc.file, uploadDoc.documentType, uploadDoc.description);
      setUploadDoc({ file: null, documentType: 'OTHER', description: '' });
      loadReg();
    } catch (err) {
      setError(err?.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center py-20 text-slate-400">
      <div className="h-6 w-6 animate-spin rounded-full border-2 border-brand-500 border-t-transparent" />
      <span className="ml-3 text-sm">Loading registration…</span>
    </div>
  );
  if (!reg) return <Alert message={error} />;

  const statusMeta = STATUS_META[reg.status] || STATUS_META.DRAFT;
  const chainMeta = reg.blockchainSyncStatus ? CHAIN_STATUS[reg.blockchainSyncStatus] : null;

  return (
    <div className="mx-auto max-w-4xl space-y-5">
      {/* Header */}
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h1 className="font-mono text-lg font-bold text-slate-900 tracking-wider">{reg.registrationRef}</h1>
            <span className={`badge border ${statusMeta.cls}`}>{statusMeta.label}</span>
            {chainMeta && (
              <span className={`badge border ${chainMeta.cls} flex items-center gap-1`}>
                <Link2 size={10} /> {chainMeta.label}
              </span>
            )}
          </div>
          <p className="mt-1 text-xs text-slate-400">
            {reg.propertyDistrict} · {reg.propertyVillage} · Survey {reg.propertySurveyNumber}
          </p>
        </div>
        <button className="btn-secondary text-sm" onClick={() => navigate('/registrations')}>
          <ArrowLeft size={14} /> Back
        </button>
      </div>

      <Alert message={error} onDismiss={() => setError('')} />

      {/* Actions */}
      {reg.status === 'PENDING_APPROVAL' && canApprove && (
        <div className="flex gap-3">
          <button className="btn-primary bg-emerald-600 hover:opacity-90" onClick={handleApprove}>
            <CheckCircle size={15} /> Approve Registration
          </button>
          <button className="btn-danger" onClick={handleReject}>
            <XCircle size={15} /> Reject
          </button>
        </div>
      )}

      {reg.rejectionReason && (
        <Alert type="error" message={`Rejection reason: ${reg.rejectionReason}`} />
      )}

      {/* Blockchain anchor info bar */}
      {reg.blockchainTxHash && (
        <div className="flex items-center gap-3 rounded-xl border border-teal-200 bg-teal-50 px-4 py-3 text-sm">
          <ShieldCheck size={16} className="shrink-0 text-teal-600" />
          <div className="min-w-0">
            <p className="font-semibold text-teal-800">Anchored on Blockchain</p>
            <p className="truncate font-mono text-xs text-teal-600">
              TX: {reg.blockchainTxHash}
              {reg.blockchainBlockNumber && ` · Block #${reg.blockchainBlockNumber}`}
            </p>
          </div>
        </div>
      )}

      {/* Tab nav */}
      <div className="tab-nav">
        {[
          { key: 'details',    label: 'Details' },
          { key: 'location',   label: reg.propertyGeometry || reg.propertyLatitude ? 'Location & Boundary' : 'Location' },
          { key: 'documents',  label: `Documents (${reg.documents?.length ?? 0})` },
          { key: 'audit',      label: `Audit Trail (${events.length})` },
        ].map(({ key, label }) => (
          <button
            key={key}
            onClick={() => setActiveTab(key)}
            className={activeTab === key ? 'tab-btn-active' : 'tab-btn'}
          >
            {label}
          </button>
        ))}
      </div>

      {/* ── Details tab ── */}
      {activeTab === 'details' && (
        <div className="space-y-5">
          {/* Property */}
          <Section title="Property Details" icon={MapPin}>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
              <Field label="Survey Number" value={reg.propertySurveyNumber} />
              <Field label="District" value={reg.propertyDistrict} />
              <Field label="Village" value={reg.propertyVillage} />
              <Field label="Area" value={`${reg.propertyAreaInAcres} acres`} />
              <Field label="Consideration Amount" value={`₹ ${Number(reg.considerationAmount || 0).toLocaleString('en-IN')}`} />
              {reg.marketValuePerAcre && <Field label="Market Rate / Acre" value={`₹ ${Number(reg.marketValuePerAcre).toLocaleString('en-IN')}`} />}
              {reg.totalMarketValue && <Field label="Total Market Value" value={`₹ ${Number(reg.totalMarketValue).toLocaleString('en-IN')}`} />}
              {reg.stampDuty && <Field label="Stamp Duty (7%)" value={`₹ ${Number(reg.stampDuty).toLocaleString('en-IN')}`} />}
            </div>
          </Section>

          {/* Parties */}
          <div className="grid gap-4 sm:grid-cols-2">
            <PartyCard title="Seller"
              name={reg.sellerName} aadhaar={reg.sellerAadhaar}
              mobile={reg.sellerMobile} email={reg.sellerEmail} address={reg.sellerAddress} />
            <PartyCard title="Buyer"
              name={reg.buyerName} aadhaar={reg.buyerAadhaar}
              mobile={reg.buyerMobile} email={reg.buyerEmail} address={reg.buyerAddress} />
          </div>

          {/* Witnesses */}
          {reg.witnesses?.length > 0 && (
            <Section title={`Witnesses (${reg.witnesses.length})`}>
              <div className="space-y-2">
                {reg.witnesses.map((w) => (
                  <div key={w.id} className="flex flex-wrap gap-6 rounded-xl bg-slate-50 px-4 py-3 text-sm">
                    <div><p className="section-label mb-0.5">#{w.sequenceNumber} Name</p><p className="font-medium">{w.name}</p></div>
                    <div><p className="section-label mb-0.5">Aadhaar</p><p className="font-mono">****{(w.aadhaarNumber || '').slice(-4)}</p></div>
                    <div><p className="section-label mb-0.5">Mobile</p><p>{w.mobile || '—'}</p></div>
                  </div>
                ))}
              </div>
            </Section>
          )}

          {reg.notes && (
            <Section title="Notes">
              <p className="text-sm text-slate-600">{reg.notes}</p>
            </Section>
          )}
        </div>
      )}

      {/* ── Location tab ── */}
      {activeTab === 'location' && (
        <div className="card space-y-4">
          <h3 className="flex items-center gap-2 font-semibold text-slate-800">
            <MapPin size={15} className="text-slate-400" /> Land Parcel Boundary
          </h3>

          {/* PLUS Code badge */}
          {reg.propertyPlusCode && (
            <div className="flex items-center gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3">
              <div>
                <p className="text-xs font-medium text-emerald-600">Govt. of India PLUS Code (Open Location Code)</p>
                <code className="mt-0.5 inline-block rounded-lg bg-emerald-100 px-3 py-1 font-mono text-lg font-bold tracking-widest text-emerald-800">
                  {reg.propertyPlusCode}
                </code>
              </div>
            </div>
          )}

          {reg.propertyGeometry || reg.propertyLatitude ? (
            <>
              {/* Coordinates row */}
              {reg.propertyLatitude && (
                <div className="flex gap-6 text-sm">
                  <div>
                    <p className="section-label mb-0.5">Centroid Latitude</p>
                    <p className="font-mono font-medium">{reg.propertyLatitude?.toFixed(6)}</p>
                  </div>
                  <div>
                    <p className="section-label mb-0.5">Centroid Longitude</p>
                    <p className="font-mono font-medium">{reg.propertyLongitude?.toFixed(6)}</p>
                  </div>
                  <div>
                    <p className="section-label mb-0.5">Location</p>
                    <p className="font-medium">{reg.propertyVillage}, {reg.propertyDistrict}</p>
                  </div>
                </div>
              )}
              <PropertyMap
                lat={reg.propertyLatitude}
                lng={reg.propertyLongitude}
                geometry={reg.propertyGeometry}
                label={`Survey ${reg.propertySurveyNumber} · ${reg.propertyVillage}`}
                height="420px"
              />
              {reg.propertyGeometry && (
                <p className="text-xs text-slate-400">
                  Polygon boundary drawn at time of registration. The highlighted area represents the land parcel.
                </p>
              )}
            </>
          ) : (
            <div className="empty-state">
              <MapPin size={32} className="opacity-30" />
              <p className="text-sm">No boundary was drawn for this registration.</p>
              <p className="text-xs">Use the polygon tool when creating a new registration to define land boundaries.</p>
            </div>
          )}
        </div>
      )}

      {/* ── Documents tab ── */}
      {activeTab === 'documents' && (
        <Section title="Documents">
          {reg.documents?.length === 0 && <p className="text-sm text-slate-400">No documents uploaded yet.</p>}
          <div className="space-y-2">
            {reg.documents?.map((doc) => (
              <div key={doc.id} className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3 text-sm">
                <div>
                  <p className="font-medium">{doc.originalName}</p>
                  <p className="text-xs text-slate-400">{doc.documentType.replace(/_/g, ' ')}{doc.description && ` · ${doc.description}`}</p>
                </div>
                <a href={doc.downloadUrl} target="_blank" rel="noreferrer" className="btn-secondary text-xs">
                  Download
                </a>
              </div>
            ))}
          </div>

          {['DRAFT', 'PENDING_APPROVAL'].includes(reg.status) && (
            <form className="mt-4 border-t border-slate-100 pt-4 space-y-3" onSubmit={handleUpload}>
              <p className="text-sm font-semibold text-slate-700">Upload Document</p>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs font-medium text-slate-600">Type</label>
                  <select className="input text-sm" value={uploadDoc.documentType}
                    onChange={(e) => setUploadDoc((p) => ({ ...p, documentType: e.target.value }))}>
                    {DOCUMENT_TYPES.map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium text-slate-600">File</label>
                  <input className="input text-sm" type="file" accept=".pdf,.jpg,.jpeg,.png"
                    onChange={(e) => setUploadDoc((p) => ({ ...p, file: e.target.files[0] }))} />
                </div>
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium text-slate-600">Description</label>
                <input className="input text-sm" value={uploadDoc.description}
                  onChange={(e) => setUploadDoc((p) => ({ ...p, description: e.target.value }))} />
              </div>
              <button className="btn-primary text-sm" disabled={!uploadDoc.file || uploading}>
                <Upload size={14} /> {uploading ? 'Uploading…' : 'Upload'}
              </button>
            </form>
          )}
        </Section>
      )}

      {/* ── Audit trail tab ── */}
      {activeTab === 'audit' && (
        <div className="card">
          <div className="mb-5 flex items-center justify-between">
            <h3 className="flex items-center gap-2 font-semibold text-slate-800">
              <ShieldCheck size={15} className="text-slate-400" />
              Blockchain Audit Trail
            </h3>
            <span className="text-xs text-slate-400">SHA-256 tamper-evident log</span>
          </div>
          <BlockchainTimeline events={events} loading={eventsLoading} />
        </div>
      )}
    </div>
  );
}
