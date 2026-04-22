import { useEffect, useState } from 'react';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const initialForm = {
  surveyNumber: '',
  district: '',
  village: '',
  areaInAcres: '',
  ownerId: '',
};

export default function LandFormPage() {
  const [form, setForm] = useState(initialForm);
  const [owners, setOwners] = useState([]);
  const [ownersLoading, setOwnersLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

  useEffect(() => {
    const loadOwners = async () => {
      try {
        const data = await landApi.getOwners();
        const records = Array.isArray(data) ? data : data.content || [];
        setOwners(records);
      } catch {
        setError('Unable to load owners. Create an owner first or retry.');
      } finally {
        setOwnersLoading(false);
      }
    };

    loadOwners();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await landApi.createLand({
        surveyNumber: form.surveyNumber.trim(),
        district: form.district.trim(),
        village: form.village.trim(),
        areaInAcres: Number(form.areaInAcres),
        ownerId: Number(form.ownerId),
      });
      setMessage('Land record saved successfully.');
      setForm(initialForm);
    } catch (err) {
      const validationErrors = err?.response?.data?.validationErrors;
      if (validationErrors) {
        setError(Object.values(validationErrors).join(' | '));
      } else {
        setError(err?.response?.data?.message || 'Unable to save land record. Please verify the form values.');
      }
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
          <label className="mb-1 block text-sm font-medium">Survey Number</label>
          <input
            className="input"
            required
            value={form.surveyNumber}
            onChange={(e) => handleChange('surveyNumber', e.target.value)}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">District</label>
          <input
            className="input"
            required
            value={form.district}
            onChange={(e) => handleChange('district', e.target.value)}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Village</label>
          <input
            className="input"
            required
            value={form.village}
            onChange={(e) => handleChange('village', e.target.value)}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Area In Acres</label>
          <input
            className="input"
            type="number"
            min="0.01"
            step="0.01"
            required
            value={form.areaInAcres}
            onChange={(e) => handleChange('areaInAcres', e.target.value)}
          />
        </div>
        <div className="md:col-span-2">
          <label className="mb-1 block text-sm font-medium">Owner</label>
          <select
            className="input"
            required
            value={form.ownerId}
            onChange={(e) => handleChange('ownerId', e.target.value)}
            disabled={ownersLoading || loading}
          >
            <option value="">Select owner</option>
            {owners.map((owner) => (
              <option key={owner.id} value={owner.id}>
                {owner.name} ({owner.nationalId})
              </option>
            ))}
          </select>
          {owners.length === 0 && !ownersLoading ? (
            <p className="mt-2 text-sm text-slate-500">No owners found yet. Create an owner record before creating a land record.</p>
          ) : null}
        </div>
        <div className="md:col-span-2 flex justify-end">
          <button className="btn-primary" disabled={loading || ownersLoading || owners.length === 0}>
            {loading ? 'Saving...' : 'Save Record'}
          </button>
        </div>
      </form>
    </div>
  );
}
