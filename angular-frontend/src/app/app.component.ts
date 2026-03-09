import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from './services/api.service';
import { LandParcel } from './models/types';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="container">
    <h1>Land Registration Portal (Angular)</h1>
    <div class="card">
      <h3>Sign in + 2FA</h3>
      <div class="grid">
        <div><input [(ngModel)]="username" placeholder="username (admin/sro/user)" /></div>
        <div><input [(ngModel)]="password" placeholder="password" type="password" /></div>
      </div>
      <button (click)="signIn()">Sign In</button>
      <input [(ngModel)]="otp" placeholder="Enter 2FA OTP" />
      <button (click)="verify2fa()">Verify 2FA</button>
      <p>{{authMsg}}</p>
      <span *ngIf="role" class="badge">Role: {{role}}</span>
    </div>

    <div class="card" *ngIf="role==='ADMIN'">
      <h3>Admin Activities</h3>
      <button (click)="loadUsers()">Load Users</button>
      <pre>{{users | json}}</pre>
      <h4>Notification Setup (Email/SMS)</h4>
      <label><input type="checkbox" [(ngModel)]="emailEnabled" /> Email</label>
      <label><input type="checkbox" [(ngModel)]="smsEnabled" /> SMS</label>
      <button (click)="saveNotifications()">Save</button>
    </div>

    <div class="card" *ngIf="role==='SRO'">
      <h3>SRO Registration Interface</h3>
      <input [(ngModel)]="landId" placeholder="Land ID" />
      <input [(ngModel)]="village" placeholder="Village" />
      <input [(ngModel)]="surveyNumber" placeholder="Survey Number" />
      <input [(ngModel)]="seller" placeholder="Seller" />
      <input [(ngModel)]="buyer" placeholder="Buyer" />
      <textarea [(ngModel)]="polygonJson" rows="4" placeholder='Coordinates JSON [{"lat":12.9,"lng":77.5},...]'></textarea>
      <button (click)="createLand()">Register Land (validate non-overlap)</button>
      <p>{{landMsg}}</p>
    </div>

    <div class="card" *ngIf="role">
      <h3>Buyer/Seller Portal</h3>
      <button (click)="loadLands()">Load Lands</button>
      <div *ngFor="let l of lands" style="border:1px solid #eee;padding:10px;margin:8px 0;border-radius:8px;">
        <b>{{l.landId}}</b> - {{l.village}} / {{l.surveyNumber}}<br>
        Seller: {{l.seller}} | Buyer: {{l.buyer}}<br>
        <button (click)="showHistory(l.landId)">Show History</button>
        <a [href]="downloadUrl(l.landId, 'DOC-SALE-DEED')" target="_blank">Download Legal Document</a>
      </div>
      <pre>{{history | json}}</pre>
      <p>Map interface available in build step using leaflet integration target; coordinate geometry is validated in backend.</p>
    </div>
  </div>
  `
})
export class AppComponent implements OnInit {
  username = 'admin'; password = 'admin123'; otp = '';
  authMsg = ''; role = '';
  users: any[] = [];
  emailEnabled = true; smsEnabled = true;
  landId='LAND-001'; village='DemoVillage'; surveyNumber='SV-100'; seller='Seller A'; buyer='Buyer B';
  polygonJson='[{"lat":12.9716,"lng":77.5946},{"lat":12.9718,"lng":77.5950},{"lat":12.9712,"lng":77.5952}]';
  landMsg=''; lands: LandParcel[]=[]; history:any[]=[];

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.api.getNotificationsConfig().subscribe(v => { this.emailEnabled=v.emailEnabled; this.smsEnabled=v.smsEnabled; }); }
  signIn() { this.api.signIn({username:this.username,password:this.password}).subscribe({next:r=>this.authMsg=`OTP:${r.demoOtp}`,error:e=>this.authMsg=e.error?.error||'failed'}); }
  verify2fa() { this.api.verify2fa({username:this.username,otp:this.otp}).subscribe({next:r=>{this.role=r.role;this.authMsg='Signed in';},error:e=>this.authMsg=e.error?.error||'failed'}); }
  loadUsers() { this.api.getUsers().subscribe(v=>this.users=v); }
  saveNotifications() { this.api.setNotificationsConfig({emailEnabled:this.emailEnabled,smsEnabled:this.smsEnabled}).subscribe(); }
  createLand() {
    const polygon = JSON.parse(this.polygonJson);
    this.api.createLand({landId:this.landId,village:this.village,surveyNumber:this.surveyNumber,seller:this.seller,buyer:this.buyer,polygon,actor:this.username})
      .subscribe({next:()=>this.landMsg='Land registered',error:e=>this.landMsg=e.error?.error||'failed'});
  }
  loadLands() { this.api.getLands().subscribe(v=>this.lands=v); }
  showHistory(id:string) { this.api.getHistory(id).subscribe(v=>this.history=v); }
  downloadUrl(landId:string, docId:string) { return `http://localhost:8080/api/platform/lands/${landId}/documents/${docId}/download`; }
}
