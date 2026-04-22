import { useState } from 'react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const initialForm = {
  name: '',
  nationalId: '',
};

export default function OwnerFormPage() {
  const [form, setForm] = useState(initialForm);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');

    try {
      const owner = await landApi.createOwner({
        name: form.name.trim(),
        nationalId: form.nationalId.trim(),
      });
      setMessage(`Owner created successfully with ID ${owner.id}.`);
      setForm(initialForm);
    } catch (err) {
      const validationErrors = err?.response?.data?.validationErrors;
      if (validationErrors) {
        setError(Object.values(validationErrors).join(' | '));
      } else {
        setError(err?.response?.data?.message || 'Unable to create owner.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl space-y-4">
      <div>
        <h2 className="text-xl font-semibold">Create Owner</h2>
        <p className="mt-1 text-sm text-slate-500">Admins can add owners here before assigning land records to them.</p>
      </div>
      <Alert type="success" message={message} />
      <Alert message={error} />
      <form className="card grid gap-4" onSubmit={handleSubmit}>
        <div>
          <label className="mb-1 block text-sm font-medium">Owner Name</label>
          <input
            className="input"
            required
            value={form.name}
            onChange={(e) => handleChange('name', e.target.value)}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">National ID</label>
          <input
            className="input"
            required
            value={form.nationalId}
            onChange={(e) => handleChange('nationalId', e.target.value)}
          />
        </div>
        <div className="flex justify-end">
          <button className="btn-primary" disabled={loading}>
            {loading ? 'Creating...' : 'Create Owner'}
          </button>
        </div>
      </form>
    </div>
  );
}
