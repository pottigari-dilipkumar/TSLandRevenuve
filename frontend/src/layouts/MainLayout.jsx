import { BarChart3, FileText, LayoutDashboard, LogOut, Receipt, ShieldCheck, PlusSquare, Menu } from 'lucide-react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

const allNavItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, roles: Object.values(ROLES) },
  { to: '/lands', label: 'Land Records', icon: FileText, roles: [ROLES.ADMIN, ROLES.REVENUE_OFFICER, ROLES.DATA_ENTRY, ROLES.CITIZEN] },
  { to: '/lands/new', label: 'Add/Edit Land', icon: PlusSquare, roles: [ROLES.ADMIN, ROLES.REVENUE_OFFICER, ROLES.DATA_ENTRY] },
  { to: '/revenue', label: 'Revenue Details', icon: Receipt, roles: [ROLES.ADMIN, ROLES.REVENUE_OFFICER] },
  { to: '/users', label: 'User Management', icon: ShieldCheck, roles: [ROLES.ADMIN] },
];

export default function MainLayout() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const navItems = allNavItems.filter((item) => item.roles.includes(user?.role));

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex min-h-screen bg-slate-100">
      <aside className={`fixed z-40 h-full w-72 bg-slate-900 p-5 text-slate-200 transition-transform lg:static ${open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}`}>
        <h2 className="text-xl font-bold text-white">LRMS Portal</h2>
        <p className="mt-1 text-xs text-slate-400">{user?.role}</p>
        <nav className="mt-6 space-y-2">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={() => setOpen(false)}
              className={({ isActive }) => `flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition ${isActive ? 'bg-brand-600 text-white' : 'text-slate-300 hover:bg-slate-800'}`}
            >
              <Icon size={16} />
              {label}
            </NavLink>
          ))}
        </nav>
        <button onClick={handleLogout} className="btn-secondary mt-8 w-full border-slate-700 bg-slate-800 text-slate-200 hover:bg-slate-700">
          <LogOut size={16} className="mr-2" /> Logout
        </button>
      </aside>

      <div className="flex min-h-screen flex-1 flex-col">
        <header className="sticky top-0 z-30 border-b border-slate-200 bg-white px-4 py-3 lg:px-8">
          <div className="flex items-center justify-between">
            <button className="btn-secondary lg:hidden" onClick={() => setOpen((prev) => !prev)}>
              <Menu size={16} />
            </button>
            <div>
              <p className="text-xs text-slate-500">Welcome back</p>
              <h1 className="text-lg font-semibold text-slate-900">{user?.name || 'User'}</h1>
            </div>
            <BarChart3 className="text-brand-600" />
          </div>
        </header>
        <main className="flex-1 p-4 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
