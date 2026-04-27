export default function StatCard({ title, value, subtitle, icon: Icon, trend, accentColor = 'brand' }) {
  const accent = {
    brand:   { bg: 'bg-brand-50',   text: 'text-brand-500',   bar: 'from-brand-400 to-brand-600' },
    emerald: { bg: 'bg-emerald-50', text: 'text-emerald-500', bar: 'from-emerald-400 to-emerald-600' },
    amber:   { bg: 'bg-amber-50',   text: 'text-amber-500',   bar: 'from-amber-400 to-amber-600' },
    rose:    { bg: 'bg-rose-50',    text: 'text-rose-500',    bar: 'from-rose-400 to-rose-600' },
    violet:  { bg: 'bg-violet-50',  text: 'text-violet-500',  bar: 'from-violet-400 to-violet-600' },
  }[accentColor] || accent.brand;

  return (
    <div className="stat-card card-hover group">
      <div className="flex items-start justify-between">
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">{title}</p>
        {Icon && (
          <div className={`flex h-8 w-8 items-center justify-center rounded-lg ${accent.bg} transition-transform group-hover:scale-110`}>
            <Icon size={15} className={accent.text} />
          </div>
        )}
      </div>
      <h3 className="mt-3 text-3xl font-bold tracking-tight text-slate-900">{value}</h3>
      <p className="mt-1 text-xs text-slate-400">{subtitle}</p>
      {trend !== undefined && (
        <div className="mt-3 flex items-center gap-1.5">
          <div className={`h-1 flex-1 rounded-full bg-gradient-to-r ${accent.bar} opacity-30`} />
          <span className={`text-xs font-semibold ${trend >= 0 ? 'text-emerald-600' : 'text-rose-500'}`}>
            {trend >= 0 ? '+' : ''}{trend}%
          </span>
        </div>
      )}
    </div>
  );
}
