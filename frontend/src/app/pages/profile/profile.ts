import { Component, inject, signal } from '@angular/core';
import { UserService } from '../../services/user-service';
import { UserResponse } from '../../models/user-response';

@Component({
  imports: [],
  selector: 'app-profile',
  styleUrl: './profile.css',
  templateUrl: './profile.html',
})
export class Profile {
  private userService = inject(UserService);

  user = signal<UserResponse | null>(null);

  loadProfile() {
    this.userService.getMe().subscribe((user) => {
      this.user.set(user);
    });
  }

  ngOnInit() {
    this.loadProfile();
  }
}
