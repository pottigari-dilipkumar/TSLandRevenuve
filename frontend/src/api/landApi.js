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
  getUsers: async () => {
    const { data } = await client.get('/users');
    return data;
  },
};
