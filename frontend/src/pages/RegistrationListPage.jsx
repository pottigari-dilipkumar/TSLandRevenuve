// Copyright (c) 2026 DivaTech. All rights reserved.
// Proprietary and confidential. Unauthorized copying or distribution is strictly prohibited.
// Website: https://www.divatech.in | Contact: legal@divatech.in

import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Eye, CheckCircle, XCircle, RotateCcw, ChevronRight } from 'lucide-react';
import { registrationApi } from '../api/registrationApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';
import Alert from '../components/Alert';

const STATUS_META = {
  DRAFT:                   { cls: 'bg-slate-100 text-slate-600',   label: 'Draft' },
  AWAITING_BUYER_APPROVAL: { cls: 'bg-blue-100 text-blue-700',     label: 'Awaiting Buyer' },
  BUYER_REJECTED:          { cls: 'bg-red-100 text-red-600',       label: 'Buyer Rejected' },
  PENDING_REVIEW:          { cls: 'bg-purple-100 text-purple-700', label: 'Under Review' },
  REVISION_REQUIRED:       { cls: 'bg-amber-100 text-amber-800',   label: 'Revision Needed' },
  PENDING_APPROVAL:        { cls: 'bg-yellow-100 text-yellow-800', label: 'Pending SRO' },
  APPROVED:                { cls: 'bg-green-100 text-green-700',   label: 'Approved' },
  REJECTED:                { cls: 'bg-red-100 text-red-700',       label: 'Rejected' },
};

const ALL_STATUSES = [
  'DRAFT', 'AWAITING_BUYER_APPROVAL', 'BUYER_REJECTED',
  'PENDING_REVIEW', 'REVISION_REQUIRED',
  'PENDING_APPROVAL', 'APPROVED', 'REJECTED',
];

export default function RegistrationListPage() {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [registrations, setRegistrations] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState('');

  const canApprove = [ROLES.SRO, ROLES.ADMIN].includes(user?.role);
  const canCreate = [ROLES.SRO, ROLES.SRO_ASSISTANT, ROLES.ADMIN].includes(user?.role);
  const isSroAssistant = [ROLES.SRO_ASSISTANT, ROLES.ADMIN].includes(user?.role);

  const load = async () => {
    setLoading(true);
    try {
      const data = await registrationApi.list(statusFilter || undefined);
      setRegistrations(data);
    } catch {
      setError('Failed to load registrations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [statusFilter]);

  const doAction = async (fn, ...args) => {
    setActionLoading(args[0]);
    try { await fn(...args); load(); }
    catch (err) { setError(err?.response?.data?.message || 'Action failed'); }
    finally { setActionLoading(''); }
  };

  const handleApprove = (ref) => doAction(registrationApi.approve, ref);
  const handleReject = async (ref) => {
    const reason = prompt('Enter rejection reason:');
    if (!reason) return;
    doAction(registrationApi.reject, ref, reason);
  };
  const handleSubmit = (ref) => doAction(registrationApi.submit, ref);
  const handleForward = (ref) => doAction(registrationApi.forwardToSro, ref);
  const handleSendBack = async (ref) => {
    const notes = prompt('Revision notes for seller:');
    if (!notes) return;
    doAction(registrationApi.sendBack, ref, notes);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-xl font-bold">Land Registrations</h1>
        <div className="flex items-center gap-3">
          <select className="input w-52" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">All Statuses</option>
            {ALL_STATUSES.map(s => (
              <option key={s} value={s}>{STATUS_META[s]?.label || s}</option>
            ))}
          </select>
          {canCreate && (
            <Link to="/registrations/new" className="btn-primary">
              <Plus size={16} /> New Registration
            </Link>
          )}
        </div>
      </div>

      <Alert message={error} onDismiss={() => setError('')} />

      {/* SRO Assistant quick-filter */}
      {isSroAssistant && (
        <div className="flex gap-2 flex-wrap">
          <button
            onClick={() => setStatusFilter('PENDING_REVIEW')}
            className={`text-xs px-3 py-1.5 rounded-full font-medium border transition-colors ${statusFilter === 'PENDING_REVIEW' ? 'bg-purple-600 text-white border-purple-600' : 'border-slate-200 text-slate-600 hover:border-purple-300'}`}
          >
            Needs Review
          </button>
          <button
            onClick={() => setStatusFilter('REVISION_REQUIRED')}
            className={`text-xs px-3 py-1.5 rounded-full font-medium border transition-colors ${statusFilter === 'REVISION_REQUIRED' ? 'bg-amber-600 text-white border-amber-600' : 'border-slate-200 text-slate-600 hover:border-amber-300'}`}
          >
            Sent Back
          </button>
          <button
            onClick={() => setStatusFilter('')}
            className="text-xs px-3 py-1.5 rounded-full font-medium border border-slate-200 text-slate-600 hover:border-slate-400 transition-colors"
          >
            All
          </button>
        </div>
      )}

      {loading ? (
        <p className="text-slate-500">Loading…</p>
      ) : registrations.length === 0 ? (
        <div className="card py-12 text-center text-slate-400">No registrations found</div>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3">Ref #</th>
                <th className="px-4 py-3">Property</th>
                <th className="px-4 py-3">Seller → Buyer</th>
                <th className="px-4 py-3">Value</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {registrations.map((reg) => {
                const meta = STATUS_META[reg.status] || STATUS_META.DRAFT;
                const busy = actionLoading === reg.registrationRef;
                return (
                  <tr key={reg.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3">
                      <span className="font-mono text-xs font-bold text-slate-800">{reg.registrationRef}</span>
                      {reg.initiatedByCitizen && (
                        <span className="ml-1 text-[10px] text-brand-600 bg-brand-50 px-1.5 py-0.5 rounded-full">Citizen</span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <p className="font-medium">{reg.propertySurveyNumber}</p>
                      <p className="text-xs text-slate-400">{reg.propertyVillage}, {reg.propertyDistrict}</p>
                    </td>
                    <td className="px-4 py-3">
                      <p className="text-xs">{reg.sellerName}</p>
                      <p className="text-xs text-slate-400">→ {reg.buyerName}</p>
                    </td>
                    <td className="px-4 py-3">
                      <p>₹{Number(reg.considerationAmount || 0).toLocaleString('en-IN')}</p>
                      {reg.stampDuty && <p className="text-xs text-slate-400">Duty: ₹{Number(reg.stampDuty).toLocaleString('en-IN')}</p>}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${meta.cls}`}>
                        {meta.label}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-400">
                      {new Date(reg.createdAt).toLocaleDateString('en-IN')}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => navigate(`/registrations/${reg.registrationRef}`)}
                          className="rounded p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
                          title="View"
                        >
                          <Eye size={15} />
                        </button>

                        {/* Staff submit DRAFT */}
                        {reg.status === 'DRAFT' && !reg.initiatedByCitizen && (
                          <button onClick={() => handleSubmit(reg.registrationRef)}
                            disabled={busy}
                            className="rounded px-2 py-0.5 text-xs bg-blue-50 text-blue-700 hover:bg-blue-100">
                            Submit
                          </button>
                        )}

                        {/* SRO Assistant: PENDING_REVIEW */}
                        {reg.status === 'PENDING_REVIEW' && isSroAssistant && (
                          <>
                            <button onClick={() => handleForward(reg.registrationRef)}
                              disabled={busy}
                              className="rounded p-1 text-purple-500 hover:bg-purple-50"
                              title="Forward to SRO">
                              <ChevronRight size={15} />
                            </button>
                            <button onClick={() => handleSendBack(reg.registrationRef)}
                              disabled={busy}
                              className="rounded p-1 text-amber-500 hover:bg-amber-50"
                              title="Send back">
                              <RotateCcw size={14} />
                            </button>
                          </>
                        )}

                        {/* SRO: PENDING_APPROVAL */}
                        {reg.status === 'PENDING_APPROVAL' && canApprove && (
                          <>
                            <button onClick={() => handleApprove(reg.registrationRef)}
                              disabled={busy}
                              className="rounded p-1 text-green-500 hover:bg-green-50"
                              title="Approve">
                              <CheckCircle size={15} />
                            </button>
                            <button onClick={() => handleReject(reg.registrationRef)}
                              disabled={busy}
                              className="rounded p-1 text-red-400 hover:bg-red-50"
                              title="Reject">
                              <XCircle size={15} />
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
