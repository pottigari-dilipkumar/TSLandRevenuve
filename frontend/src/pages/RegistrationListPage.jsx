import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Eye, CheckCircle, XCircle } from 'lucide-react';
import { registrationApi } from '../api/registrationApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';
import Alert from '../components/Alert';

const STATUS_COLORS = {
  DRAFT: 'bg-slate-100 text-slate-700',
  PENDING_APPROVAL: 'bg-yellow-100 text-yellow-800',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
};

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

  const handleApprove = async (ref) => {
    setActionLoading(ref);
    try {
      await registrationApi.approve(ref);
      load();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to approve');
    } finally {
      setActionLoading('');
    }
  };

  const handleReject = async (ref) => {
    const reason = prompt('Enter rejection reason:');
    if (!reason) return;
    setActionLoading(ref);
    try {
      await registrationApi.reject(ref, reason);
      load();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to reject');
    } finally {
      setActionLoading('');
    }
  };

  const handleSubmit = async (ref) => {
    setActionLoading(ref);
    try {
      await registrationApi.submit(ref);
      load();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to submit');
    } finally {
      setActionLoading('');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">Land Registrations</h1>
        <div className="flex items-center gap-3">
          <select className="input w-48" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">All Statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="PENDING_APPROVAL">Pending Approval</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
          {canCreate && (
            <Link to="/registrations/new" className="btn-primary">
              <Plus size={16} className="mr-1 inline" /> New Registration
            </Link>
          )}
        </div>
      </div>

      <Alert message={error} />

      {loading ? (
        <p className="text-slate-500">Loading...</p>
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
              {registrations.map((reg) => (
                <tr key={reg.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-mono text-xs font-bold">{reg.registrationRef}</td>
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
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[reg.status]}`}>
                      {reg.status.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-slate-400">
                    {new Date(reg.createdAt).toLocaleDateString('en-IN')}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1">
                      <button onClick={() => navigate(`/registrations/${reg.registrationRef}`)}
                        className="rounded p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700">
                        <Eye size={15} />
                      </button>
                      {reg.status === 'DRAFT' && (
                        <button onClick={() => handleSubmit(reg.registrationRef)}
                          disabled={actionLoading === reg.registrationRef}
                          className="rounded px-2 py-0.5 text-xs bg-blue-50 text-blue-700 hover:bg-blue-100">
                          Submit
                        </button>
                      )}
                      {reg.status === 'PENDING_APPROVAL' && canApprove && (
                        <>
                          <button onClick={() => handleApprove(reg.registrationRef)}
                            disabled={actionLoading === reg.registrationRef}
                            className="rounded p-1 text-green-500 hover:bg-green-50">
                            <CheckCircle size={15} />
                          </button>
                          <button onClick={() => handleReject(reg.registrationRef)}
                            disabled={actionLoading === reg.registrationRef}
                            className="rounded p-1 text-red-400 hover:bg-red-50">
                            <XCircle size={15} />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
