import { Component } from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { inject } from '@angular/core';
import { AuthService } from '../../services/auth';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  private authService = inject(AuthService);

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required]),
  });

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { email, password } = this.loginForm.getRawValue();

    if (!email || !password) {
      return;
    }

    this.authService.login(email, password).subscribe({
      next: (response) => {
        console.log('Login successful');
      },
      error: (error) => {
        console.error('Login failed:', error.status);
      },
    });
  }
}
