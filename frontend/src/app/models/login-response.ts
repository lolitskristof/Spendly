import { UserRole } from './user-role';

export interface LoginResponse {
  id: number;
  displayName: string;
  email: string;
  role: UserRole;
  token: string;
}
