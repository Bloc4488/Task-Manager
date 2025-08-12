import { Component } from '@angular/core';
import {
  RouterOutlet,
  RouterLink,
  Router,
  NavigationStart,
  NavigationCancel,
  NavigationEnd,
  NavigationError
} from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from './shared/auth.service';
import {ThemeService} from './shared/theme.service';
import {AsyncPipe} from '@angular/common';
import {LoadingService} from './shared/loading.service';
import {LoadingComponent} from './shared/loading/loading';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatToolbarModule, MatButtonModule, RouterLink, AsyncPipe, LoadingComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(
    public authService: AuthService,
    public themeService: ThemeService,
    private router: Router,
    private loadingService: LoadingService
  ) {}

  ngOnInit() {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.loadingService.show();
      } else if (
        event instanceof NavigationEnd ||
        event instanceof NavigationCancel ||
        event instanceof NavigationError
      ) {
        this.loadingService.hide();
      }
    })
  }
}
