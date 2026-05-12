// Copyright (c) 2026 DivaTech. All rights reserved.
// Proprietary and confidential. Unauthorized copying or distribution is strictly prohibited.
// Website: https://www.divatech.in | Contact: legal@divatech.in

import client from './client';

export const notificationApi = {
  getAll:         ()       => client.get('/notifications').then(r => r.data),
  getUnreadCount: ()       => client.get('/notifications/unread-count').then(r => r.data.count),
  markRead:       (id)     => client.put(`/notifications/${id}/read`),
  markAllRead:    ()       => client.put('/notifications/read-all'),
};
