import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Alert from '../components/Alert';
import { landApi } from '../api/landApi';

const LAND_TYPES = ['PRIVATE', 'GOVERNMENT', 'FOREST', 'ASSIGNED', 'INAM', 'WAQF', 'NALA_CONVERTED'];

const initialForm = {
  surveyNumber: '',
  district: '',
  village: '',
  areaInAcres: '',
  ownerId: '',
  landType: 'PRIVATE',
  passbookNumber: '',
};

export default function LandFormPage() {
  const { id } = useParams();           // present when editing an existing record
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState(initialForm);
  const [owners, setOwners] = useState([]);
  const [ownersLoading, setOwnersLoading] = useState(true);
  const [recordLoading, setRecordLoading] = useState(isEdit);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

  // Load owners list
  useEffect(() => {
    landApi.getOwners()
      .then((data) => {
        const records = Array.isArray(data) ? data : data.content || [];
        setOwners(records);
      })
      .catch(() => setError('Unable to load owners. Create an owner first.'))
      .finally(() => setOwnersLoading(false));
  }, []);

  // Load existing record when editing
  useEffect(() => {
    if (!isEdit) return;
    setRecordLoading(true);
    landApi.getLandById(id)
      .then((rec) => {
        setForm({
          surveyNumber: rec.surveyNumber || '',
          district: rec.district || '',
          village: rec.village || '',
          areaInAcres: rec.areaInAcres ?? '',
          ownerId: rec.ownerId ?? '',
          landType: rec.landType || 'PRIVATE',
          passbookNumber: rec.passbookNumber || '',
        });
      })
      .catch(() => setError('Failed to load land record.'))
      .finally(() => setRecordLoading(false));
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    const payload = {
      surveyNumber: form.surveyNumber.trim(),
      district: form.district.trim(),
      village: form.village.trim(),
      areaInAcres: Number(form.areaInAcres),
      ownerId: Number(form.ownerId),
      landType: form.landType || 'PRIVATE',
      passbookNumber: form.passbookNumber.trim() || null,
    };
    try {
      if (isEdit) {
        await landApi.updateLand(id, payload);
        setMessage('Land record updated successfully.');
      } else {
        await landApi.createLand(payload);
        setMessage('Land record created successfully.');
        setForm(initialForm);
      }
    } catch (err) {
      const ve = err?.response?.data?.validationErrors;
      setError(ve ? Object.values(ve).join(' | ') : err?.response?.data?.message || 'Unable to save land record.');
    } finally {
      setLoading(false);
    }
  };

  const isDataLoading = ownersLoading || recordLoading;

  return (
    <div className="max-w-3xl space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">{isEdit ? 'Edit Land Record' : 'Add Land Record'}</h2>
        {isEdit && (
          <button className="btn-secondary text-sm" onClick={() => navigate('/lands')}>
            ← Back to Records
          </button>
        )}
      </div>

      <Alert type="success" message={message} />
      <Alert message={error} />

      {isDataLoading ? (
        <p className="text-slate-500">Loading...</p>
      ) : (
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
          <div>
            <label className="mb-1 block text-sm font-medium">Land Type</label>
            <select
              className="input"
              value={form.landType}
              onChange={(e) => handleChange('landType', e.target.value)}
            >
              {LAND_TYPES.map((t) => (
                <option key={t} value={t}>{t.replace('_', ' ')}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Passbook Number (PPB)</label>
            <input
              className="input"
              placeholder="Optional"
              value={form.passbookNumber}
              onChange={(e) => handleChange('passbookNumber', e.target.value)}
            />
          </div>
          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium">Owner</label>
            <select
              className="input"
              required
              value={form.ownerId}
              onChange={(e) => handleChange('ownerId', e.target.value)}
              disabled={loading}
            >
              <option value="">Select owner</option>
              {owners.map((owner) => (
                <option key={owner.id} value={owner.id}>
                  {owner.name} ({owner.nationalId})
                </option>
              ))}
            </select>
            {owners.length === 0 && (
              <p className="mt-2 text-sm text-slate-500">
                No owners found. Create an owner record first.
              </p>
            )}
          </div>
          <div className="md:col-span-2 flex justify-end gap-3">
            {isEdit && (
              <button type="button" className="btn-secondary" onClick={() => navigate('/lands')}>
                Cancel
              </button>
            )}
            <button className="btn-primary" disabled={loading || owners.length === 0}>
              {loading ? 'Saving...' : isEdit ? 'Update Record' : 'Save Record'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
