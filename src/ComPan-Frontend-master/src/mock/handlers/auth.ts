import Mock from 'mockjs';

export const setupAuthMocks = () => {
  Mock.mock(/\/api\/v1\/auth\/login/, 'post', {
    success: true,
    code: 200,
    message: 'Login successful',
    data: { 
      token: '@guid', 
      expiresIn: 3600, 
      user: { 
        userId: 'user1', 
        username: 'Demo User', 
        email: 'demo@example.com' 
      } 
    },
  });
}; 