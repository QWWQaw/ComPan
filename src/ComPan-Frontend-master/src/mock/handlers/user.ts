import Mock from 'mockjs';
import { vfsApi } from '../vfs'; // We might not need vfs here, but good to have.

const users = [
  { userId: 'user1', username: 'Alice', email: 'alice@example.com' },
  { userId: 'user2', username: 'Bob', email: 'bob@example.com' },
  { userId: 'user3', username: 'Charlie', email: 'charlie@example.com' },
  { userId: 'user4', username: 'David', email: 'david@example.com' },
];

export const setupUserMocks = () => {
  // Get Users (with search)
  Mock.mock(/\/api\/v1\/users/, 'get', (options) => {
    const url = new URL(options.url, 'http://localhost');
    const search = url.searchParams.get('search') || '';
    
    const filteredUsers = users.filter(user => 
      user.username.toLowerCase().includes(search.toLowerCase()) ||
      user.email.toLowerCase().includes(search.toLowerCase())
    );

    return {
      success: true,
      code: 200,
      data: {
        items: filteredUsers,
        pagination: { totalItems: filteredUsers.length, totalPages: 1, perPage: filteredUsers.length, currentPage: 1 },
      },
    };
  });
}; 