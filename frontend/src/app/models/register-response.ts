import { UserRole } from './userRole';

export default interface RegisterResponse {
  id: number;
  displayName: string;
  email: string;
  userRole: UserRole;
}
