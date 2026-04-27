import client from './client';

export const landApi = {
  getDashboardStats: async () => {
    const { data } = await client.get('/dashboard/stats');
    return data;
  },
  getRevenueTrend: async () => {
    const { data } = await client.get('/revenue/trend');
    return data;
  },
  getLandRecords: async () => {
    const { data } = await client.get('/lands');
    return data;
  },
  getOwners: async () => {
    const { data } = await client.get('/owners');
    return data;
  },
  createOwner: async (payload) => {
    const { data } = await client.post('/owners', payload);
    return data;
  },
  getLandById: async (id) => {
    const { data } = await client.get(`/lands/${id}`);
    return data;
  },
  getLandHistory: async (id) => {
    const { data } = await client.get(`/lands/${id}/history`);
    return data;
  },
  createLand: async (payload) => {
    const { data } = await client.post('/lands', payload);
    return data;
  },
  updateLand: async (id, payload) => {
    const { data } = await client.put(`/lands/${id}`, payload);
    return data;
  },
  getRevenueDetails: async () => {
    const { data } = await client.get('/revenue');
    return data;
  },
  getUsers: async ({ type = 'staff', page = 0, size = 10 } = {}) => {
    const { data } = await client.get('/users', { params: { type, page, size } });
    return data;
  },
  createUser: async (payload) => {
    const { data } = await client.post('/users', payload);
    return data;
  },

  // Mutations
  getMutations: async ({ status, page = 0, size = 20 } = {}) => {
    const params = { page, size };
    if (status) params.status = status;
    const { data } = await client.get('/mutations', { params });
    return data;
  },
  getMutationsByLand: async (landRecordId, { page = 0, size = 20 } = {}) => {
    const { data } = await client.get(`/mutations/land/${landRecordId}`, { params: { page, size } });
    return data;
  },
  getMutation: async (ref) => {
    const { data } = await client.get(`/mutations/${ref}`);
    return data;
  },
  applyMutation: async (payload) => {
    const { data } = await client.post('/mutations', payload);
    return data;
  },
  sendMutationToReview: async (ref) => {
    const { data } = await client.post(`/mutations/${ref}/review`);
    return data;
  },
  approveMutation: async (ref, payload) => {
    const { data } = await client.post(`/mutations/${ref}/approve`, payload);
    return data;
  },
  rejectMutation: async (ref, payload) => {
    const { data } = await client.post(`/mutations/${ref}/reject`, payload);
    return data;
  },

  // Public (no auth)
  publicSearch: async ({ district, village, surveyNumber } = {}) => {
    const { data } = await client.get('/public/search', { params: { district, village, surveyNumber } });
    return data;
  },
  getEC: async ({ district, village, surveyNumber }) => {
    const { data } = await client.get('/public/ec', { params: { district, village, surveyNumber } });
    return data;
  },
};
