export default function Alert({ type = 'error', message }) {
  if (!message) return null;

  const styleByType = {
    error: 'border-red-200 bg-red-50 text-red-700',
    success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    info: 'border-blue-200 bg-blue-50 text-blue-700',
  };

  return <div className={`rounded-lg border px-3 py-2 text-sm ${styleByType[type]}`}>{message}</div>;
}
