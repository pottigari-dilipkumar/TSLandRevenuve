import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';

const config = {
  error:   { cls: 'bg-red-50 border-red-200 text-red-700',         Icon: AlertCircle,    iconCls: 'text-red-500' },
  success: { cls: 'bg-emerald-50 border-emerald-200 text-emerald-700', Icon: CheckCircle2, iconCls: 'text-emerald-500' },
  info:    { cls: 'bg-brand-50 border-brand-100 text-brand-700',   Icon: Info,           iconCls: 'text-brand-400' },
  warning: { cls: 'bg-amber-50 border-amber-200 text-amber-700',   Icon: AlertCircle,    iconCls: 'text-amber-500' },
};

export default function Alert({ type = 'error', message, onDismiss }) {
  if (!message) return null;
  const { cls, Icon, iconCls } = config[type] || config.error;

  return (
    <div className={`flex items-start gap-3 rounded-xl border px-4 py-3 text-sm font-medium animate-fade-in ${cls}`}>
      <Icon size={16} className={`mt-0.5 shrink-0 ${iconCls}`} />
      <span className="flex-1">{message}</span>
      {onDismiss && (
        <button onClick={onDismiss} className="shrink-0 opacity-60 hover:opacity-100 transition-opacity">
          <X size={14} />
        </button>
      )}
    </div>
  );
}
