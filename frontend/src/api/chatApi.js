// Copyright (c) 2026 DivaTech. All rights reserved.
// Proprietary and confidential. Unauthorized copying or distribution is strictly prohibited.
// Website: https://www.divatech.in | Contact: legal@divatech.in

import client from './client';

export const sendChatMessage = async (message) => {
  const { data } = await client.post('/chat/message', { message });
  return data;
};

export const sendPublicChatMessage = async (message) => {
  const { data } = await client.post('/public/chat/message', { message });
  return data;
};

export const getChatHistory = async () => {
  const { data } = await client.get('/chat/history');
  return data;
};

export const clearChatHistory = async () => {
  await client.delete('/chat/history');
};
