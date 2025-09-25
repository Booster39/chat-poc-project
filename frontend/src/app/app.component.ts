import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <div class="app-container">
      <h1>{{title}}</h1>
       <app-chat></app-chat> 
    </div>
  `,
  styles: [`
    .app-container {
      margin: 0;
      padding: 0;
      min-height: 100vh;
      background-color: #ecf0f1;
    }
    
    h1 {
      text-align: center;
      padding: 20px;
      color: #2c3e50;
      margin: 0;
    }
  `]
})
export class AppComponent {
  title = 'Chat Service Client - PoC';
}