import client from './client';

export const authApi = {
  login: async (payload) => {
    const { data } = await client.post('/auth/login', payload);
    return data;
  },
  register: async (payload) => {
    const { data } = await client.post('/auth/register', payload);
    return data;
  },
};
