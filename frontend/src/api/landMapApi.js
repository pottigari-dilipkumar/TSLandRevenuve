import client from './client';

export const getParcels = async () => {
  const { data } = await client.get('/public/map/parcels');
  return data;
};

export const searchByCoordinates = async (lat, lng) => {
  const { data } = await client.get('/public/map/search', { params: { lat, lng } });
  return data;
};

export const getLandHistory = async (id) => {
  const { data } = await client.get(`/public/map/${id}/history`);
  return data;
};
