import { Link } from 'react-router-dom';
import { FileSearch } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="flex min-h-[70vh] flex-col items-center justify-center gap-4 text-center">
      <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-brand-50">
        <FileSearch size={36} className="text-brand-400" />
      </div>
      <div>
        <h1 className="text-6xl font-extrabold text-slate-200">404</h1>
        <p className="mt-2 text-lg font-semibold text-slate-700">Page not found</p>
        <p className="mt-1 text-sm text-slate-400">The page you're looking for doesn't exist or was moved.</p>
      </div>
      <Link to="/dashboard" className="btn-primary mt-2">Back to Dashboard</Link>
    </div>
  );
}
