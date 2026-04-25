import client from './client';

export const registrationApi = {
  createDraft: async (payload) => {
    const { data } = await client.post('/registrations', payload);
    return data;
  },
  list: async (status) => {
    const params = status ? { status } : {};
    const { data } = await client.get('/registrations', { params });
    return data;
  },
  getByRef: async (ref) => {
    const { data } = await client.get(`/registrations/${ref}`);
    return data;
  },
  submit: async (ref) => {
    const { data } = await client.put(`/registrations/${ref}/submit`);
    return data;
  },
  approve: async (ref) => {
    const { data } = await client.put(`/registrations/${ref}/approve`);
    return data;
  },
  reject: async (ref, reason) => {
    const { data } = await client.put(`/registrations/${ref}/reject`, { reason });
    return data;
  },
};
