import client from './client';

export const notificationApi = {
  getAll:         ()       => client.get('/notifications').then(r => r.data),
  getUnreadCount: ()       => client.get('/notifications/unread-count').then(r => r.data.count),
  markRead:       (id)     => client.put(`/notifications/${id}/read`),
  markAllRead:    ()       => client.put('/notifications/read-all'),
};
