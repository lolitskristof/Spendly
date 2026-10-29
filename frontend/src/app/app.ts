import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './services/auth-service';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  private authService = inject(AuthService);

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  logout(): void {
    this.authService.logout();
  }

  protected readonly title = signal('frontend');
}
