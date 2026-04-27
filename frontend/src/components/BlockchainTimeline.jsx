import { CheckCircle2, FileEdit, Send, XCircle, Upload, Link2, Clock } from 'lucide-react';

const EVENT_META = {
  DRAFTED:            { icon: FileEdit,    color: 'bg-brand-100 text-brand-600',   label: 'Draft Created' },
  SUBMITTED:          { icon: Send,         color: 'bg-amber-100 text-amber-600',   label: 'Submitted for Approval' },
  APPROVED:           { icon: CheckCircle2, color: 'bg-emerald-100 text-emerald-600', label: 'Approved' },
  REJECTED:           { icon: XCircle,      color: 'bg-rose-100 text-rose-600',     label: 'Rejected' },
  DOCUMENT_UPLOADED:  { icon: Upload,       color: 'bg-violet-100 text-violet-600', label: 'Document Uploaded' },
  BLOCKCHAIN_ANCHORED:{ icon: Link2,        color: 'bg-teal-100 text-teal-600',     label: 'Blockchain Anchored' },
};

function truncate(str, n = 18) {
  if (!str) return '—';
  return str.length > n ? str.slice(0, n) + '…' : str;
}

function fmt(ts) {
  if (!ts) return '—';
  return new Date(ts).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

function ChainBadge({ status }) {
  if (!status) return null;
  const styles = {
    SYNCED:  'bg-emerald-50 text-emerald-700 border-emerald-200',
    FAILED:  'bg-rose-50 text-rose-700 border-rose-200',
    SKIPPED: 'bg-slate-50 text-slate-500 border-slate-200',
  };
  return (
    <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[10px] font-semibold ${styles[status] || styles.SKIPPED}`}>
      {status}
    </span>
  );
}

export default function BlockchainTimeline({ events = [], loading }) {
  if (loading) {
    return (
      <div className="flex items-center gap-2 py-6 text-sm text-slate-400">
        <div className="h-4 w-4 animate-spin rounded-full border-2 border-brand-400 border-t-transparent" />
        Loading audit trail…
      </div>
    );
  }

  if (!events.length) {
    return <p className="py-6 text-center text-sm text-slate-400">No audit events recorded yet.</p>;
  }

  return (
    <ol className="relative space-y-0">
      {events.map((ev, i) => {
        const meta = EVENT_META[ev.eventType] || { icon: Clock, color: 'bg-slate-100 text-slate-500', label: ev.eventType };
        const Icon = meta.icon;
        const isLast = i === events.length - 1;

        return (
          <li key={ev.id} className="relative flex gap-4 pb-6">
            {/* Vertical connector */}
            {!isLast && (
              <div className="absolute left-4 top-8 bottom-0 w-px bg-slate-200" />
            )}

            {/* Icon */}
            <div className={`relative z-10 flex h-8 w-8 shrink-0 items-center justify-center rounded-full ${meta.color}`}>
              <Icon size={14} />
            </div>

            {/* Content */}
            <div className="flex-1 pt-0.5">
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-sm font-semibold text-slate-800">{meta.label}</span>
                {ev.chainSyncStatus && <ChainBadge status={ev.chainSyncStatus} />}
              </div>
              <p className="mt-0.5 text-xs text-slate-500">{ev.details}</p>

              <div className="mt-2 flex flex-wrap gap-4 text-[11px] text-slate-400">
                <span className="flex items-center gap-1">
                  <Clock size={10} /> {fmt(ev.timestamp)}
                </span>
                {ev.actorUsername && ev.actorUsername !== 'SYSTEM' && (
                  <span>By <span className="font-medium text-slate-600">{ev.actorUsername}</span></span>
                )}
                {ev.txHash && (
                  <span className="flex items-center gap-1">
                    <Link2 size={10} />
                    <span className="font-mono">{truncate(ev.txHash, 22)}</span>
                    {ev.blockNumber && <span className="text-slate-300">· block #{ev.blockNumber}</span>}
                  </span>
                )}
              </div>

              {/* Payload hash fingerprint */}
              <div className="mt-1.5 flex items-center gap-1 font-mono text-[10px] text-slate-300">
                <span title="SHA-256 event fingerprint">#{ev.sequence} · {ev.payloadHash?.slice(0, 16)}…</span>
              </div>
            </div>
          </li>
        );
      })}
    </ol>
  );
}
