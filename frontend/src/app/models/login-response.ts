export default interface LoginResponse {
  id: number;
  displayName: string;
  email: string;
  role: UserRole;
  token: string;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER',
}
