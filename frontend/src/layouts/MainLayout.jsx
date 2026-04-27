import {
  BarChart3, FileText, LayoutDashboard, LogOut, Receipt, ShieldCheck,
  PlusSquare, Menu, UserPlus, ClipboardList, User, TrendingUp, X, ChevronRight,
  GitBranch, Search, FileCheck,
} from 'lucide-react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { ROLES } from '../utils/roles';

const allNavItems = [
  { to: '/dashboard',         label: 'Dashboard',       icon: LayoutDashboard, roles: [ROLES.ADMIN, ROLES.REVENUE_OFFICER, ROLES.DATA_ENTRY] },
  { to: '/citizen/dashboard', label: 'My Dashboard',    icon: LayoutDashboard, roles: [ROLES.CITIZEN] },
  { to: '/citizen/profile',   label: 'My Profile',      icon: User,            roles: [ROLES.CITIZEN] },
  { to: '/market-values',     label: 'Market Values',   icon: TrendingUp,      roles: Object.values(ROLES) },
  { to: '/lands',             label: 'Land Records',    icon: FileText,        roles: [ROLES.ADMIN, ROLES.REVENUE_OFFICER, ROLES.DATA_ENTRY] },
  { to: '/owners/new',        label: 'Create Owner',    icon: UserPlus,        roles: [ROLES.ADMIN] },
  { to: '/lands/new',         label: 'Add Land Record', icon: PlusSquare,      roles: [ROLES.ADMIN, ROLES.DATA_ENTRY] },
  { to: '/revenue',           label: 'Revenue Details', icon: Receipt,         roles: [ROLES.ADMIN, ROLES.REVENUE_OFFICER] },
  { to: '/registrations',     label: 'Registrations',   icon: ClipboardList,   roles: [ROLES.SRO, ROLES.SRO_ASSISTANT, ROLES.ADMIN, ROLES.REVENUE_OFFICER] },
  { to: '/registrations/new', label: 'New Registration',icon: PlusSquare,      roles: [ROLES.SRO, ROLES.SRO_ASSISTANT, ROLES.ADMIN] },
  { to: '/mutations',         label: 'Mutations',        icon: GitBranch,       roles: [ROLES.ADMIN, ROLES.REVENUE_OFFICER, ROLES.DATA_ENTRY, ROLES.CITIZEN] },
  { to: '/mutations/new',     label: 'Apply Mutation',   icon: PlusSquare,      roles: [ROLES.CITIZEN, ROLES.DATA_ENTRY] },
  { to: '/public/search',     label: 'Property Search',  icon: Search,          roles: Object.values(ROLES) },
  { to: '/public/ec',         label: 'Encumbrance Cert', icon: FileCheck,       roles: Object.values(ROLES) },
  { to: '/users',             label: 'User Management',  icon: ShieldCheck,     roles: [ROLES.ADMIN] },
];

function NavItem({ to, label, icon: Icon, onClick }) {
  return (
    <NavLink
      to={to}
      onClick={onClick}
      end={to === '/dashboard' || to === '/citizen/dashboard'}
      className={({ isActive }) =>
        `group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all duration-200 ${
          isActive
            ? 'bg-white/10 text-white shadow-sm'
            : 'text-slate-400 hover:bg-white/5 hover:text-white'
        }`
      }
    >
      {({ isActive }) => (
        <>
          <span className={`flex h-7 w-7 items-center justify-center rounded-lg transition-all ${
            isActive ? 'bg-brand-500/30' : 'group-hover:bg-white/5'
          }`}>
            <Icon size={15} />
          </span>
          <span className="flex-1">{label}</span>
          {isActive && <ChevronRight size={13} className="opacity-60" />}
        </>
      )}
    </NavLink>
  );
}

function UserAvatar({ name }) {
  const initials = name.split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase();
  return (
    <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-gradient text-sm font-bold text-white shadow-sm">
      {initials}
    </div>
  );
}

export default function MainLayout() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const navItems = allNavItems.filter((item) => item.roles.includes(user?.role));
  const displayName = user?.fullName || user?.username || 'User';
  const roleLabel = user?.role?.replace(/_/g, ' ') || '';

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="flex min-h-screen bg-page-gradient">

      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 z-30 bg-black/50 backdrop-blur-sm lg:hidden"
          onClick={() => setOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={`
        fixed z-40 flex h-full w-64 flex-col bg-dark-gradient px-4 py-5
        transition-transform duration-300 lg:static lg:translate-x-0
        ${open ? 'translate-x-0' : '-translate-x-full'}
      `}>
        {/* Logo */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-gradient shadow-glow-sm">
              <BarChart3 size={18} className="text-white" />
            </div>
            <div>
              <p className="text-sm font-bold text-white leading-none">LRMS</p>
              <p className="text-[10px] text-slate-500 mt-0.5">Land Revenue Portal</p>
            </div>
          </div>
          <button className="btn-icon lg:hidden text-slate-400" onClick={() => setOpen(false)}>
            <X size={16} />
          </button>
        </div>

        {/* User card */}
        <div className="mt-6 flex items-center gap-3 rounded-xl bg-white/5 px-3 py-2.5">
          <UserAvatar name={displayName} />
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-white">{displayName}</p>
            <p className="text-[10px] font-medium uppercase tracking-wider text-slate-500">{roleLabel}</p>
          </div>
        </div>

        {/* Nav */}
        <nav className="mt-6 flex-1 space-y-0.5 overflow-y-auto">
          <p className="section-label mb-2 px-3 text-slate-600">Navigation</p>
          {navItems.map(({ to, label, icon }) => (
            <NavItem key={to} to={to} label={label} icon={icon} onClick={() => setOpen(false)} />
          ))}
        </nav>

        {/* Logout */}
        <button
          onClick={handleLogout}
          className="mt-4 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-slate-400 transition-all hover:bg-white/5 hover:text-red-400"
        >
          <span className="flex h-7 w-7 items-center justify-center rounded-lg">
            <LogOut size={15} />
          </span>
          Sign Out
        </button>
      </aside>

      {/* Main */}
      <div className="flex min-h-screen flex-1 flex-col overflow-hidden">
        {/* Top bar */}
        <header className="sticky top-0 z-20 flex items-center gap-4 border-b border-slate-200/80 bg-white/80 px-5 py-3.5 shadow-sm backdrop-blur-md lg:px-8">
          <button className="btn-icon lg:hidden" onClick={() => setOpen(true)}>
            <Menu size={18} />
          </button>
          <div className="flex-1">
            <p className="text-xs text-slate-400">Welcome back,</p>
            <h1 className="text-base font-semibold text-slate-900 leading-tight">{displayName}</h1>
          </div>
          <div className="hidden sm:flex items-center gap-2 rounded-xl bg-brand-50 px-3 py-1.5">
            <span className="text-xs font-semibold text-brand-600">{roleLabel}</span>
          </div>
        </header>

        {/* Content */}
        <main className="flex-1 p-5 lg:p-8 animate-fade-in">
          <Outlet />
        </main>

        {/* Footer */}
        <footer className="px-8 py-3 text-center text-xs text-slate-400 border-t border-slate-200/60">
          LRMS Portal &mdash; Land Revenue &amp; Registration System
        </footer>
      </div>
    </div>
  );
}
