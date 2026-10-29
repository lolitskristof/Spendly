import { UserRole } from './userRole';

export default interface LoginResponse {
  id: number;
  displayName: string;
  email: string;
  role: UserRole;
  token: string;
}
