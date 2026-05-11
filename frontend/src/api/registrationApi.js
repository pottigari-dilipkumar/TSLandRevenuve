import client from './client';

export const registrationApi = {
  // Staff
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
  getEvents: async (ref) => {
    const { data } = await client.get(`/registrations/${ref}/events`);
    return data;
  },

  // Citizen: seller flow
  createSaleRequest: async (payload) => {
    const { data } = await client.post('/registrations/sale-request', payload);
    return data;
  },
  sellerSubmit: async (ref) => {
    const { data } = await client.put(`/registrations/${ref}/seller-submit`);
    return data;
  },
  sellerResubmit: async (ref) => {
    const { data } = await client.put(`/registrations/${ref}/seller-resubmit`);
    return data;
  },

  // Citizen: buyer consent
  buyerApprove: async (ref) => {
    const { data } = await client.put(`/registrations/${ref}/buyer-approve`);
    return data;
  },
  buyerReject: async (ref, reason) => {
    const { data } = await client.put(`/registrations/${ref}/buyer-reject`, { reason });
    return data;
  },

  // SRO Assistant
  sendBack: async (ref, notes) => {
    const { data } = await client.put(`/registrations/${ref}/send-back`, { notes });
    return data;
  },
  forwardToSro: async (ref) => {
    const { data } = await client.put(`/registrations/${ref}/forward-to-sro`);
    return data;
  },

  // Citizen queues
  getMySales: async () => {
    const { data } = await client.get('/registrations/my-sales');
    return data;
  },
  getPendingMyApproval: async () => {
    const { data } = await client.get('/registrations/pending-my-approval');
    return data;
  },
};
