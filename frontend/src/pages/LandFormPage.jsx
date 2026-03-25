import { useState } from 'react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

export default function LandFormPage() {
  const [form, setForm] = useState({ plotNumber: '', ownerName: '', area: '', village: '', classification: 'Agricultural' });
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
      await landApi.createLand(form);
      setMessage('Land record saved successfully.');
      setForm({ plotNumber: '', ownerName: '', area: '', village: '', classification: 'Agricultural' });
    } catch {
      setError('Unable to save land record. Please verify payload or backend endpoint.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl space-y-4">
      <h2 className="text-xl font-semibold">Add / Edit Land Record</h2>
      <Alert type="success" message={message} />
      <Alert message={error} />
      <form className="card grid gap-4 md:grid-cols-2" onSubmit={handleSubmit}>
        <div>
          <label className="mb-1 block text-sm font-medium">Plot Number</label>
          <input className="input" required value={form.plotNumber} onChange={(e) => handleChange('plotNumber', e.target.value)} />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Owner Name</label>
          <input className="input" required value={form.ownerName} onChange={(e) => handleChange('ownerName', e.target.value)} />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Area</label>
          <input className="input" required value={form.area} onChange={(e) => handleChange('area', e.target.value)} />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Village</label>
          <input className="input" required value={form.village} onChange={(e) => handleChange('village', e.target.value)} />
        </div>
        <div className="md:col-span-2">
          <label className="mb-1 block text-sm font-medium">Classification</label>
          <select className="input" value={form.classification} onChange={(e) => handleChange('classification', e.target.value)}>
            <option>Agricultural</option>
            <option>Residential</option>
            <option>Industrial</option>
          </select>
        </div>
        <div className="md:col-span-2 flex justify-end">
          <button className="btn-primary" disabled={loading}>{loading ? 'Saving...' : 'Save Record'}</button>
        </div>
      </form>
    </div>
  );
}
