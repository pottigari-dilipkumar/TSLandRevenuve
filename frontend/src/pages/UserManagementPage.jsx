import { useEffect, useState } from 'react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const fallback = [
  { id: 'U-01', name: 'Admin User', email: 'admin@lrms.gov', role: 'ADMIN', status: 'Active' },
  { id: 'U-02', name: 'Revenue Officer 1', email: 'officer@lrms.gov', role: 'REVENUE_OFFICER', status: 'Active' },
  { id: 'U-03', name: 'Data Operator', email: 'entry@lrms.gov', role: 'DATA_ENTRY', status: 'Active' },
];

export default function UserManagementPage() {
  const [users, setUsers] = useState(fallback);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadUsers = async () => {
      try {
        const data = await landApi.getUsers();
        const list = Array.isArray(data) ? data : data.users;
        setUsers(list?.length ? list : fallback);
      } catch {
        setError('Unable to load live user list. Showing sample users.');
      }
    };

    loadUsers();
  }, []);

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold">User Management</h2>
      <Alert type="info" message={error} />
      <div className="card overflow-x-auto p-0">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-4 py-3 text-left">ID</th>
              <th className="px-4 py-3 text-left">Name</th>
              <th className="px-4 py-3 text-left">Email</th>
              <th className="px-4 py-3 text-left">Role</th>
              <th className="px-4 py-3 text-left">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {users.map((user) => (
              <tr key={user.id}>
                <td className="px-4 py-3">{user.id}</td>
                <td className="px-4 py-3">{user.name}</td>
                <td className="px-4 py-3">{user.email}</td>
                <td className="px-4 py-3">{user.role}</td>
                <td className="px-4 py-3">{user.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
