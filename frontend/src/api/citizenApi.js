import client from './client';

export const citizenApi = {
  getMyLands: async () => {
    const { data } = await client.get('/citizen/lands');
    return data;
  },
  getMyRegistrations: async () => {
    const { data } = await client.get('/citizen/registrations');
    return data;
  },
};

export const marketValueApi = {
  getAll: async (district) => {
    const params = district ? { district } : {};
    const { data } = await client.get('/market-values', { params });
    return data;
  },
  lookup: async (district, village) => {
    const { data } = await client.get('/market-values/lookup', { params: { district, village } });
    return data;
  },
};

export const documentApi = {
  upload: async (registrationRef, file, documentType, description) => {
    const form = new FormData();
    form.append('registrationRef', registrationRef);
    form.append('file', file);
    form.append('documentType', documentType);
    if (description) form.append('description', description);
    const { data } = await client.post('/documents/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data;
  },
};
