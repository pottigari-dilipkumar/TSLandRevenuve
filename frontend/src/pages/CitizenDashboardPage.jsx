// Copyright (c) 2026 DivaTech. All rights reserved.
// Proprietary and confidential. Unauthorized copying or distribution is strictly prohibited.
// Website: https://www.divatech.in | Contact: legal@divatech.in

import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FileText, MapPin, TrendingUp, User, PlusCircle, Clock, CheckCircle2, AlertTriangle } from 'lucide-react';
import { citizenApi } from '../api/citizenApi';
import { registrationApi } from '../api/registrationApi';
import { useAuthStore } from '../store/authStore';
import Alert from '../components/Alert';

const STATUS_META = {
  DRAFT:                   { cls: 'bg-slate-100 text-slate-600',     label: 'Draft' },
  AWAITING_BUYER_APPROVAL: { cls: 'bg-blue-100 text-blue-700',       label: 'Awaiting Buyer' },
  BUYER_REJECTED:          { cls: 'bg-red-100 text-red-700',         label: 'Buyer Rejected' },
  PENDING_REVIEW:          { cls: 'bg-purple-100 text-purple-700',   label: 'Under Review' },
  REVISION_REQUIRED:       { cls: 'bg-amber-100 text-amber-800',     label: 'Revision Required' },
  PENDING_APPROVAL:        { cls: 'bg-yellow-100 text-yellow-800',   label: 'Pending SRO' },
  APPROVED:                { cls: 'bg-green-100 text-green-700',     label: 'Approved' },
  REJECTED:                { cls: 'bg-red-100 text-red-700',         label: 'Rejected' },
};

function RegistrationCard({ reg, showAction }) {
  const navigate = useNavigate();
  const meta = STATUS_META[reg.status] || STATUS_META.DRAFT;
  const needsAttention = ['REVISION_REQUIRED', 'AWAITING_BUYER_APPROVAL', 'BUYER_REJECTED'].includes(reg.status);

  return (
    <div
      className={`card flex items-start justify-between gap-4 cursor-pointer hover:shadow-card-hover transition-shadow ${needsAttention ? 'border-l-4 border-l-amber-400' : ''}`}
      onClick={() => navigate(`/registrations/${reg.registrationRef}`)}
    >
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap">
          <span className="font-mono text-sm font-semibold text-slate-800">{reg.registrationRef}</span>
          <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${meta.cls}`}>
            {meta.label}
          </span>
          {needsAttention && <AlertTriangle size={13} className="text-amber-500" />}
        </div>
        <p className="mt-1 text-sm text-slate-600 truncate">
          {reg.propertySurveyNumber} — {reg.propertyVillage}, {reg.propertyDistrict}
        </p>
        <p className="text-xs text-slate-400 mt-0.5">
          {reg.sellerName} → {reg.buyerName}
          {reg.considerationAmount && ` · ₹${Number(reg.considerationAmount).toLocaleString('en-IN')}`}
        </p>
        {reg.revisionNotes && reg.status === 'REVISION_REQUIRED' && (
          <p className="mt-1 text-xs text-amber-700 bg-amber-50 rounded px-2 py-1">
            Revision needed: {reg.revisionNotes}
          </p>
        )}
      </div>
      <div className="text-xs text-slate-400 whitespace-nowrap flex-shrink-0">
        {new Date(reg.createdAt).toLocaleDateString('en-IN')}
      </div>
    </div>
  );
}

export default function CitizenDashboardPage() {
  const { user } = useAuthStore();
  const [lands, setLands] = useState([]);
  const [mySales, setMySales] = useState([]);
  const [pendingApprovals, setPendingApprovals] = useState([]);
  const [allRegistrations, setAllRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const [landsData, salesData, pendingData, regsData] = await Promise.all([
          citizenApi.getMyLands(),
          registrationApi.getMySales().catch(() => []),
          registrationApi.getPendingMyApproval().catch(() => []),
          citizenApi.getMyRegistrations().catch(() => []),
        ]);
        setLands(landsData);
        setMySales(salesData);
        setPendingApprovals(pendingData);
        setAllRegistrations(regsData);
      } catch {
        setError('Failed to load your dashboard');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const totalEstimatedValue = lands.reduce((sum, l) => sum + (l.estimatedMarketValue || 0), 0);
  const activeCount = mySales.filter(r => !['APPROVED', 'REJECTED', 'BUYER_REJECTED'].includes(r.status)).length;

  if (loading) return <div className="p-8 text-center text-slate-500">Loading your dashboard…</div>;

  return (
    <div className="space-y-8">
      <Alert message={error} />

      {/* Profile completion banner */}
      {!user?.profileComplete && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 flex items-center justify-between">
          <div>
            <p className="font-medium text-amber-900">Complete your profile</p>
            <p className="text-sm text-amber-700">Add your name, mobile, and Aadhaar to use all features.</p>
          </div>
          <Link to="/citizen/profile" className="btn-primary text-sm">Complete Profile</Link>
        </div>
      )}

      {/* Pending buyer approvals — prominent alert */}
      {pendingApprovals.length > 0 && (
        <div className="rounded-xl border border-blue-200 bg-blue-50 p-4">
          <div className="flex items-center gap-2 mb-3">
            <Clock size={16} className="text-blue-600" />
            <p className="font-semibold text-blue-900">
              {pendingApprovals.length} sale request{pendingApprovals.length > 1 ? 's' : ''} awaiting your consent
            </p>
          </div>
          <div className="space-y-2">
            {pendingApprovals.map(reg => (
              <Link
                key={reg.registrationRef}
                to={`/registrations/${reg.registrationRef}`}
                className="flex items-center justify-between bg-white rounded-lg px-4 py-2.5 text-sm hover:bg-blue-50 transition-colors"
              >
                <div>
                  <span className="font-mono font-semibold text-slate-800">{reg.registrationRef}</span>
                  <span className="text-slate-500 ml-2">· {reg.sellerName} wants to sell you land in {reg.propertyVillage}</span>
                </div>
                <span className="text-blue-600 font-semibold text-xs">Review →</span>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Summary cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-4">
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-brand-50 p-3"><MapPin className="text-brand-600" size={20} /></div>
            <div>
              <p className="text-xs text-slate-500">My Properties</p>
              <p className="text-2xl font-bold">{lands.length}</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-green-50 p-3"><TrendingUp className="text-green-600" size={20} /></div>
            <div>
              <p className="text-xs text-slate-500">Estimated Value</p>
              <p className="text-2xl font-bold">₹{(totalEstimatedValue / 1e7).toFixed(2)}Cr</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-purple-50 p-3"><FileText className="text-purple-600" size={20} /></div>
            <div>
              <p className="text-xs text-slate-500">My Sales</p>
              <p className="text-2xl font-bold">{mySales.length}</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-amber-50 p-3"><Clock className="text-amber-600" size={20} /></div>
            <div>
              <p className="text-xs text-slate-500">Active</p>
              <p className="text-2xl font-bold">{activeCount + pendingApprovals.length}</p>
            </div>
          </div>
        </div>
      </div>

      {/* My Lands */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold">My Land Holdings</h2>
          {lands.length > 0 && (
            <Link to="/registrations/sale-request/new" className="btn-primary text-sm">
              <PlusCircle size={14} /> Initiate Sale
            </Link>
          )}
        </div>
        {lands.length === 0 ? (
          <div className="card text-center text-slate-500 py-8">No land records linked to your Aadhaar.</div>
        ) : (
          <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-4 py-3">Survey No.</th>
                  <th className="px-4 py-3">Location</th>
                  <th className="px-4 py-3">Area</th>
                  <th className="px-4 py-3">Market Rate / Acre</th>
                  <th className="px-4 py-3">Estimated Value</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {lands.map((land) => (
                  <tr key={land.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-mono font-medium">{land.surveyNumber}</td>
                    <td className="px-4 py-3">{land.village}, {land.district}</td>
                    <td className="px-4 py-3">{land.areaInAcres} acres</td>
                    <td className="px-4 py-3">
                      {land.marketValuePerAcre > 0
                        ? `₹${Number(land.marketValuePerAcre).toLocaleString('en-IN')}`
                        : <span className="text-slate-400">N/A</span>}
                    </td>
                    <td className="px-4 py-3 font-medium text-green-700">
                      {land.estimatedMarketValue > 0
                        ? `₹${Number(land.estimatedMarketValue).toLocaleString('en-IN')}`
                        : <span className="text-slate-400">N/A</span>}
                    </td>
                    <td className="px-4 py-3">
                      <Link
                        to={`/registrations/sale-request/new?landId=${land.id}`}
                        className="text-xs text-brand-600 hover:underline font-medium"
                        onClick={e => e.stopPropagation()}
                      >
                        Sell
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* My Sale Requests (as seller) */}
      {mySales.length > 0 && (
        <section>
          <h2 className="mb-4 text-lg font-semibold">My Sale Requests</h2>
          <div className="space-y-3">
            {mySales.map(reg => <RegistrationCard key={reg.registrationRef} reg={reg} />)}
          </div>
        </section>
      )}

      {/* All registrations (buyer/seller/witness) */}
      {allRegistrations.length > 0 && (
        <section>
          <h2 className="mb-4 text-lg font-semibold">All My Registrations</h2>
          <div className="space-y-3">
            {allRegistrations.filter(r => !mySales.find(s => s.registrationRef === r.registrationRef)).map(reg => (
              <RegistrationCard key={reg.registrationRef} reg={reg} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
