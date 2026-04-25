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
  // Citizen Aadhaar-based auth
  citizenSendOtp: async (aadhaarNumber) => {
    const { data } = await client.post('/citizen/auth/send-otp', { aadhaarNumber });
    return data;
  },
  citizenVerifyOtp: async (aadhaarNumber, otp) => {
    const { data } = await client.post('/citizen/auth/verify-otp', { aadhaarNumber, otp });
    return data;
  },
  citizenUpdateProfile: async (payload) => {
    const { data } = await client.put('/citizen/auth/profile', payload);
    return data;
  },
  citizenGetProfile: async () => {
    const { data } = await client.get('/citizen/auth/profile');
    return data;
  },
};
