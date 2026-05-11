import { useState, useEffect, useRef, useCallback } from 'react';
import { Bell, X, CheckCheck } from 'lucide-react';
import { notificationApi } from '../api/notificationApi';

const TYPE_COLOR = {
  SALE_REQUEST_RECEIVED:  'bg-blue-500',
  BUYER_CONSENTED:        'bg-green-500',
  BUYER_REJECTED:         'bg-red-500',
  REVISION_REQUIRED:      'bg-amber-500',
  REVIEW_REQUESTED:       'bg-purple-500',
  PENDING_FINAL_APPROVAL: 'bg-indigo-500',
  RESUBMITTED:            'bg-teal-500',
  REGISTRATION_APPROVED:  'bg-emerald-500',
  REGISTRATION_REJECTED:  'bg-red-600',
};

function timeAgo(isoString) {
  const diff = Date.now() - new Date(isoString).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

export default function NotificationBell() {
  const [open, setOpen]         = useState(false);
  const [items, setItems]       = useState([]);
  const [unread, setUnread]     = useState(0);
  const [loading, setLoading]   = useState(false);
  const panelRef                = useRef(null);

  const fetchCount = useCallback(async () => {
    try {
      const count = await notificationApi.getUnreadCount();
      setUnread(count);
    } catch { /* silently ignore */ }
  }, []);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    try {
      const data = await notificationApi.getAll();
      setItems(data);
      setUnread(data.filter(n => !n.read).length);
    } catch { /* silently ignore */ }
    finally { setLoading(false); }
  }, []);

  // Poll unread count every 30 s
  useEffect(() => {
    fetchCount();
    const id = setInterval(fetchCount, 30000);
    return () => clearInterval(id);
  }, [fetchCount]);

  // Load full list when panel opens
  useEffect(() => {
    if (open) fetchAll();
  }, [open, fetchAll]);

  // Close on outside click
  useEffect(() => {
    if (!open) return;
    const handler = (e) => {
      if (panelRef.current && !panelRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [open]);

  const handleMarkRead = async (id) => {
    await notificationApi.markRead(id);
    setItems(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
    setUnread(prev => Math.max(0, prev - 1));
  };

  const handleMarkAll = async () => {
    await notificationApi.markAllRead();
    setItems(prev => prev.map(n => ({ ...n, read: true })));
    setUnread(0);
  };

  return (
    <div className="relative" ref={panelRef}>
      {/* Bell button */}
      <button
        onClick={() => setOpen(v => !v)}
        className="relative flex h-9 w-9 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-600 shadow-sm transition hover:bg-slate-50 hover:text-slate-800"
        aria-label="Notifications"
      >
        <Bell size={17} />
        {unread > 0 && (
          <span className="absolute -right-1 -top-1 flex h-4 min-w-[1rem] items-center justify-center rounded-full bg-red-500 px-0.5 text-[10px] font-bold text-white leading-none">
            {unread > 99 ? '99+' : unread}
          </span>
        )}
      </button>

      {/* Dropdown panel */}
      {open && (
        <div className="absolute right-0 top-11 z-50 w-80 rounded-2xl border border-slate-200 bg-white shadow-xl sm:w-96 animate-fade-in">
          {/* Header */}
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
            <span className="text-sm font-semibold text-slate-800">Notifications</span>
            <div className="flex items-center gap-2">
              {unread > 0 && (
                <button
                  onClick={handleMarkAll}
                  className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-medium text-brand-600 hover:bg-brand-50 transition"
                >
                  <CheckCheck size={13} />
                  Mark all read
                </button>
              )}
              <button onClick={() => setOpen(false)} className="rounded-lg p-1 text-slate-400 hover:bg-slate-100">
                <X size={14} />
              </button>
            </div>
          </div>

          {/* List */}
          <div className="max-h-96 overflow-y-auto divide-y divide-slate-50">
            {loading && (
              <div className="py-8 text-center text-sm text-slate-400">Loading…</div>
            )}
            {!loading && items.length === 0 && (
              <div className="py-8 text-center text-sm text-slate-400">No notifications yet</div>
            )}
            {!loading && items.map(n => (
              <button
                key={n.id}
                onClick={() => !n.read && handleMarkRead(n.id)}
                className={`w-full text-left px-4 py-3 transition hover:bg-slate-50 ${n.read ? 'opacity-60' : ''}`}
              >
                <div className="flex items-start gap-3">
                  <span className={`mt-0.5 h-2 w-2 flex-shrink-0 rounded-full ${TYPE_COLOR[n.type] ?? 'bg-slate-400'}`} />
                  <div className="min-w-0 flex-1">
                    <p className={`text-xs font-semibold text-slate-800 ${!n.read ? 'text-slate-900' : ''}`}>{n.title}</p>
                    <p className="mt-0.5 text-xs text-slate-500 leading-snug line-clamp-2">{n.message}</p>
                    <p className="mt-1 text-[10px] text-slate-400">{timeAgo(n.createdAt)}</p>
                  </div>
                  {!n.read && <span className="mt-1.5 h-1.5 w-1.5 flex-shrink-0 rounded-full bg-brand-500" />}
                </div>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
