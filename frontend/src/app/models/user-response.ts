export interface UserResponse {
  id: number;
  displayName: string;
  email: string;
  role: 'USER' | 'ADMIN';
}
