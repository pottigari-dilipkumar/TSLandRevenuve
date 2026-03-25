import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3">
      <h1 className="text-3xl font-bold">404</h1>
      <p className="text-slate-500">Page not found</p>
      <Link to="/dashboard" className="btn-primary">Back to Dashboard</Link>
    </div>
  );
}
