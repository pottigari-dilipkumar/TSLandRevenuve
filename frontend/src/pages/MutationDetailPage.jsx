import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { CheckCircle2, XCircle, ArrowRight, User, MapPin, Calendar } from 'lucide-react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

const STATUS_COLORS = {
  APPLIED: 'bg-blue-100 text-blue-700 border-blue-200',
  MANDAL_REVIEW: 'bg-amber-100 text-amber-700 border-amber-200',
  APPROVED: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  REJECTED: 'bg-red-100 text-red-700 border-red-200',
};

function Field({ label, value }) {
  if (!value) return null;
  return (
    <div>
      <p className="text-xs text-slate-400">{label}</p>
      <p className="text-sm font-medium text-slate-700">{value}</p>
    </div>
  );
}

export default function MutationDetailPage() {
  const { ref } = useParams();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [mutation, setMutation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [rejectReason, setRejectReason] = useState('');
  const [showReject, setShowReject] = useState(false);

  const canDecide = [ROLES.ADMIN, ROLES.REVENUE_OFFICER].includes(user?.role);

  const load = () => {
    landApi.getMutation(ref).then(setMutation).catch(() => setError('Mutation not found.')).finally(() => setLoading(false));
  };

  useEffect(load, [ref]);

  const doReview = async () => {
    setActionLoading(true);
    try { setMutation(await landApi.sendMutationToReview(ref)); }
    catch (e) { setError(e?.response?.data?.message || 'Action failed.'); }
    finally { setActionLoading(false); }
  };

  const doApprove = async () => {
    setActionLoading(true);
    try { setMutation(await landApi.approveMutation(ref, {})); }
    catch (e) { setError(e?.response?.data?.message || 'Action failed.'); }
    finally { setActionLoading(false); }
  };

  const doReject = async () => {
    setActionLoading(true);
    try { setMutation(await landApi.rejectMutation(ref, { rejectionReason: rejectReason })); setShowReject(false); }
    catch (e) { setError(e?.response?.data?.message || 'Action failed.'); }
    finally { setActionLoading(false); }
  };

  if (loading) return (
    <div className="flex items-center justify-center py-20 text-slate-400">
      <div className="h-6 w-6 animate-spin rounded-full border-2 border-brand-500 border-t-transparent" />
      <span className="ml-3 text-sm">Loading…</span>
    </div>
  );

  if (!mutation) return <Alert message={error || 'Mutation not found.'} />;

  const isSuccession = mutation.mutationType === 'SUCCESSION';

  return (
    <div className="max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs text-slate-400 font-mono">{mutation.mutationRef}</p>
          <h2 className="page-title">{mutation.mutationType.replace('_', ' ')} Mutation</h2>
        </div>
        <div className="flex items-center gap-3">
          <span className={`rounded-full border px-3 py-1 text-xs font-semibold ${STATUS_COLORS[mutation.status]}`}>
            {mutation.status}
          </span>
          <button className="btn-secondary text-sm" onClick={() => navigate('/mutations')}>← Back</button>
        </div>
      </div>

      <Alert message={error} />

      {/* Ownership transfer card */}
      <div className="card flex items-center gap-4">
        <div className="flex-1 rounded-xl bg-slate-50 p-4">
          <p className="text-xs font-medium text-slate-400 mb-1">Previous Owner</p>
          <p className="font-semibold text-slate-700">{mutation.previousOwnerName}</p>
          {mutation.previousOwnerAadhaar && <p className="text-xs text-slate-400 font-mono mt-0.5">xxxx-xxxx-{mutation.previousOwnerAadhaar.slice(-4)}</p>}
        </div>
        <ArrowRight size={20} className="flex-shrink-0 text-slate-300" />
        <div className="flex-1 rounded-xl bg-brand-50 p-4">
          <p className="text-xs font-medium text-brand-400 mb-1">New Owner</p>
          <p className="font-semibold text-slate-700">{mutation.newOwnerName}</p>
          {mutation.newOwnerAadhaar && <p className="text-xs text-slate-400 font-mono mt-0.5">xxxx-xxxx-{mutation.newOwnerAadhaar.slice(-4)}</p>}
        </div>
      </div>

      {/* Details */}
      <div className="card grid gap-4 md:grid-cols-2">
        <Field label="Land Record ID" value={`#${mutation.landRecordId}`} />
        <Field label="Registration Ref" value={mutation.registrationRef} />
        <Field label="New Owner Mobile" value={mutation.newOwnerMobile} />
        <Field label="New Owner Email" value={mutation.newOwnerEmail} />
        {mutation.newOwnerAddress && (
          <div className="md:col-span-2">
            <p className="text-xs text-slate-400">New Owner Address</p>
            <p className="text-sm text-slate-700">{mutation.newOwnerAddress}</p>
          </div>
        )}
        {isSuccession && (
          <>
            <Field label="Relation to Deceased" value={mutation.relationToDeceased} />
            <Field label="Date of Death" value={mutation.dateOfDeath} />
          </>
        )}
        {mutation.remarks && (
          <div className="md:col-span-2">
            <p className="text-xs text-slate-400">Remarks</p>
            <p className="text-sm text-slate-700">{mutation.remarks}</p>
          </div>
        )}
        {mutation.rejectionReason && (
          <div className="md:col-span-2 rounded-lg bg-red-50 p-3">
            <p className="text-xs font-medium text-red-600">Rejection Reason</p>
            <p className="text-sm text-red-700">{mutation.rejectionReason}</p>
          </div>
        )}
      </div>

      {/* Timeline */}
      <div className="card space-y-2">
        <h3 className="section-label">Timeline</h3>
        {mutation.appliedAt && <div className="flex gap-3 text-sm"><Calendar size={14} className="mt-0.5 text-slate-400" /><span className="text-slate-500">Applied on</span><span>{new Date(mutation.appliedAt).toLocaleString()}</span></div>}
        {mutation.reviewedAt && <div className="flex gap-3 text-sm"><Calendar size={14} className="mt-0.5 text-amber-400" /><span className="text-slate-500">Sent for review</span><span>{new Date(mutation.reviewedAt).toLocaleString()}</span></div>}
        {mutation.decidedAt && <div className="flex gap-3 text-sm"><Calendar size={14} className="mt-0.5 text-emerald-400" /><span className="text-slate-500">Decision on</span><span>{new Date(mutation.decidedAt).toLocaleString()}</span></div>}
      </div>

      {/* Actions */}
      {canDecide && mutation.status !== 'APPROVED' && mutation.status !== 'REJECTED' && (
        <div className="card space-y-4">
          <h3 className="section-label">Actions</h3>
          <div className="flex flex-wrap gap-3">
            {mutation.status === 'APPLIED' && (
              <button className="btn-secondary" disabled={actionLoading} onClick={doReview}>
                Send to Mandal Review
              </button>
            )}
            {mutation.status === 'MANDAL_REVIEW' && (
              <>
                <button className="btn-primary flex items-center gap-2" disabled={actionLoading} onClick={doApprove}>
                  <CheckCircle2 size={15} /> Approve
                </button>
                <button className="btn-danger flex items-center gap-2" disabled={actionLoading} onClick={() => setShowReject(true)}>
                  <XCircle size={15} /> Reject
                </button>
              </>
            )}
          </div>

          {showReject && (
            <div className="space-y-2 rounded-xl border border-red-200 bg-red-50 p-4">
              <label className="text-sm font-medium text-red-700">Rejection Reason</label>
              <textarea className="input resize-none" rows={2} value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)} placeholder="State the reason for rejection" />
              <div className="flex gap-2">
                <button className="btn-danger text-sm" disabled={actionLoading || !rejectReason.trim()} onClick={doReject}>
                  Confirm Rejection
                </button>
                <button className="btn-secondary text-sm" onClick={() => setShowReject(false)}>Cancel</button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
