// Copyright (c) 2026 DivaTech. All rights reserved.
// Proprietary and confidential. Unauthorized copying or distribution is strictly prohibited.
// Website: https://www.divatech.in | Contact: legal@divatech.in

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
