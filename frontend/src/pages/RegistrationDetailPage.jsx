import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { CheckCircle, XCircle, Upload } from 'lucide-react';
import { registrationApi } from '../api/registrationApi';
import { documentApi } from '../api/citizenApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';
import Alert from '../components/Alert';

const DOCUMENT_TYPES = ['TITLE_DEED', 'ENCUMBRANCE_CERTIFICATE', 'ID_PROOF', 'PHOTOGRAPH', 'POWER_OF_ATTORNEY', 'SALE_AGREEMENT', 'TAX_RECEIPT', 'OTHER'];

const STATUS_COLORS = {
  DRAFT: 'bg-slate-100 text-slate-700',
  PENDING_APPROVAL: 'bg-yellow-100 text-yellow-800',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
};

export default function RegistrationDetailPage() {
  const { ref } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [reg, setReg] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [uploadDoc, setUploadDoc] = useState({ file: null, documentType: 'OTHER', description: '' });
  const [uploading, setUploading] = useState(false);

  const canApprove = [ROLES.SRO, ROLES.ADMIN].includes(user?.role);

  const load = async () => {
    try {
      const data = await registrationApi.getByRef(ref);
      setReg(data);
    } catch {
      setError('Registration not found');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [ref]);

  const handleApprove = async () => {
    try {
      await registrationApi.approve(ref);
      load();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to approve');
    }
  };

  const handleReject = async () => {
    const reason = prompt('Enter rejection reason:');
    if (!reason) return;
    try {
      await registrationApi.reject(ref, reason);
      load();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to reject');
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!uploadDoc.file) return;
    setUploading(true);
    try {
      await documentApi.upload(ref, uploadDoc.file, uploadDoc.documentType, uploadDoc.description);
      setUploadDoc({ file: null, documentType: 'OTHER', description: '' });
      load();
    } catch (err) {
      setError(err?.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  if (loading) return <div className="p-8 text-center text-slate-500">Loading...</div>;
  if (!reg) return <Alert message={error} />;

  const Section = ({ title, children }) => (
    <div className="card">
      <h3 className="mb-4 font-semibold text-slate-800">{title}</h3>
      {children}
    </div>
  );

  const Field = ({ label, value }) => (
    <div>
      <p className="text-xs text-slate-400">{label}</p>
      <p className="font-medium">{value || '—'}</p>
    </div>
  );

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold font-mono">{reg.registrationRef}</h1>
          <span className={`mt-1 inline-block rounded-full px-3 py-0.5 text-sm font-medium ${STATUS_COLORS[reg.status]}`}>
            {reg.status.replace('_', ' ')}
          </span>
        </div>
        <button className="btn-secondary" onClick={() => navigate('/registrations')}>← Back</button>
      </div>

      <Alert message={error} />

      {/* Approval actions */}
      {reg.status === 'PENDING_APPROVAL' && canApprove && (
        <div className="flex gap-3">
          <button className="btn-primary flex items-center gap-2 bg-green-600 hover:bg-green-700" onClick={handleApprove}>
            <CheckCircle size={16} /> Approve Registration
          </button>
          <button className="btn-secondary flex items-center gap-2 border-red-200 text-red-600 hover:bg-red-50" onClick={handleReject}>
            <XCircle size={16} /> Reject
          </button>
        </div>
      )}

      {reg.rejectionReason && (
        <div className="rounded-lg bg-red-50 border border-red-200 p-4 text-sm text-red-800">
          <strong>Rejection Reason:</strong> {reg.rejectionReason}
        </div>
      )}

      {/* Property */}
      <Section title="Property Details">
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          <Field label="Survey Number" value={reg.propertySurveyNumber} />
          <Field label="District" value={reg.propertyDistrict} />
          <Field label="Village" value={reg.propertyVillage} />
          <Field label="Area" value={`${reg.propertyAreaInAcres} acres`} />
          <Field label="Consideration Amount" value={`₹${Number(reg.considerationAmount || 0).toLocaleString('en-IN')}`} />
          {reg.marketValuePerAcre && <Field label="Market Rate/Acre" value={`₹${Number(reg.marketValuePerAcre).toLocaleString('en-IN')}`} />}
          {reg.totalMarketValue && <Field label="Total Market Value" value={`₹${Number(reg.totalMarketValue).toLocaleString('en-IN')}`} />}
          {reg.stampDuty && <Field label="Stamp Duty (7%)" value={`₹${Number(reg.stampDuty).toLocaleString('en-IN')}`} />}
        </div>
      </Section>

      {/* Seller & Buyer */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Section title="Seller">
          <div className="space-y-3">
            <Field label="Name" value={reg.sellerName} />
            <Field label="Aadhaar" value={`****${(reg.sellerAadhaar || '').slice(-4)}`} />
            <Field label="Mobile" value={reg.sellerMobile} />
            <Field label="Email" value={reg.sellerEmail} />
            <Field label="Address" value={reg.sellerAddress} />
          </div>
        </Section>
        <Section title="Buyer">
          <div className="space-y-3">
            <Field label="Name" value={reg.buyerName} />
            <Field label="Aadhaar" value={`****${(reg.buyerAadhaar || '').slice(-4)}`} />
            <Field label="Mobile" value={reg.buyerMobile} />
            <Field label="Email" value={reg.buyerEmail} />
            <Field label="Address" value={reg.buyerAddress} />
          </div>
        </Section>
      </div>

      {/* Witnesses */}
      {reg.witnesses?.length > 0 && (
        <Section title={`Witnesses (${reg.witnesses.length})`}>
          <div className="space-y-3">
            {reg.witnesses.map((w) => (
              <div key={w.id} className="flex gap-6 rounded-lg bg-slate-50 p-3 text-sm">
                <div><p className="text-xs text-slate-400">Name</p><p className="font-medium">{w.name}</p></div>
                <div><p className="text-xs text-slate-400">Aadhaar</p><p className="font-medium">****{(w.aadhaarNumber || '').slice(-4)}</p></div>
                <div><p className="text-xs text-slate-400">Mobile</p><p>{w.mobile}</p></div>
              </div>
            ))}
          </div>
        </Section>
      )}

      {/* Documents */}
      <Section title="Documents">
        {reg.documents?.length === 0 && <p className="text-sm text-slate-400 mb-4">No documents uploaded yet.</p>}
        <div className="space-y-2 mb-4">
          {reg.documents?.map((doc) => (
            <div key={doc.id} className="flex items-center justify-between rounded-lg bg-slate-50 p-3 text-sm">
              <div>
                <p className="font-medium">{doc.originalName}</p>
                <p className="text-xs text-slate-400">{doc.documentType.replace(/_/g, ' ')} {doc.description && `· ${doc.description}`}</p>
              </div>
              <a href={doc.downloadUrl} target="_blank" rel="noreferrer"
                className="btn-secondary text-xs">Download</a>
            </div>
          ))}
        </div>

        {/* Upload form */}
        {['DRAFT', 'PENDING_APPROVAL'].includes(reg.status) && (
          <form className="border-t border-slate-100 pt-4 space-y-3" onSubmit={handleUpload}>
            <p className="text-sm font-medium">Upload Document</p>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="mb-1 block text-xs font-medium">Type</label>
                <select className="input text-sm" value={uploadDoc.documentType}
                  onChange={(e) => setUploadDoc((p) => ({ ...p, documentType: e.target.value }))}>
                  {DOCUMENT_TYPES.map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                </select>
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium">File</label>
                <input className="input text-sm" type="file" accept=".pdf,.jpg,.jpeg,.png"
                  onChange={(e) => setUploadDoc((p) => ({ ...p, file: e.target.files[0] }))} />
              </div>
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium">Description</label>
              <input className="input text-sm" value={uploadDoc.description}
                onChange={(e) => setUploadDoc((p) => ({ ...p, description: e.target.value }))} />
            </div>
            <button className="btn-primary text-sm" disabled={!uploadDoc.file || uploading}>
              <Upload size={14} className="mr-1 inline" />{uploading ? 'Uploading...' : 'Upload'}
            </button>
          </form>
        )}
      </Section>

      {reg.notes && (
        <Section title="Notes">
          <p className="text-sm text-slate-600">{reg.notes}</p>
        </Section>
      )}
    </div>
  );
}
