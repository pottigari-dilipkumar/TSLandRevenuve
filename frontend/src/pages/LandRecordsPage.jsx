import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

const fallback = [
  { id: 'LR-1001', owner: 'Aarav Patel', village: 'Haripura', area: '1.2 ha', status: 'Verified' },
  { id: 'LR-1002', owner: 'Neha Singh', village: 'Rampur', area: '0.9 ha', status: 'Pending' },
];

export default function LandRecordsPage() {
  const user = useAuthStore((state) => state.user);
  const [rows, setRows] = useState(fallback);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const data = await landApi.getLandRecords();
        const records = Array.isArray(data) ? data : data.content || data.records;
        setRows(records?.length ? records : fallback);
      } catch {
        setError('Could not load live land records. Showing sample records.');
      }
    };

    loadData();
  }, []);

  const canCreateOrUpdate = [ROLES.ADMIN, ROLES.DATA_ENTRY].includes(user?.role);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">Land Records</h2>
        {canCreateOrUpdate && (
          <Link to="/lands/new" className="btn-primary">
            Add / Edit Land
          </Link>
        )}
      </div>
      {user?.role === ROLES.CITIZEN && <Alert type="info" message="Showing only land records mapped to your account." />}
      <Alert type="info" message={error} />
      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left">
            <tr>
              <th className="px-4 py-3">Record ID</th>
              <th className="px-4 py-3">Owner</th>
              <th className="px-4 py-3">Village</th>
              <th className="px-4 py-3">Area</th>
              <th className="px-4 py-3">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {rows.map((row) => (
              <tr key={row.id}>
                <td className="px-4 py-3 font-medium text-brand-700">{row.id}</td>
                <td className="px-4 py-3">{row.ownerName || row.owner}</td>
                <td className="px-4 py-3">{row.village}</td>
                <td className="px-4 py-3">{row.areaInAcres || row.area}</td>
                <td className="px-4 py-3">{row.status || 'Available'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
