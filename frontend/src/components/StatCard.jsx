export default function StatCard({ title, value, subtitle }) {
  return (
    <div className="card">
      <p className="text-sm text-slate-500">{title}</p>
      <h3 className="mt-2 text-2xl font-semibold">{value}</h3>
      <p className="mt-1 text-xs text-slate-400">{subtitle}</p>
    </div>
  );
}
