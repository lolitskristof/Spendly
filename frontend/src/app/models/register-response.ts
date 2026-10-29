import { UserRole } from './user-role';

export interface RegisterResponse {
  id: number;
  displayName: string;
  email: string;
  userRole: UserRole;
}
