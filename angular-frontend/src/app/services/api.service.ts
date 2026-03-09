import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly base = 'http://localhost:8080/api/platform';
  constructor(private http: HttpClient) {}

  signIn(payload: { username: string; password: string }): Observable<any> {
    return this.http.post(`${this.base}/auth/sign-in`, payload);
  }
  verify2fa(payload: { username: string; otp: string }): Observable<any> {
    return this.http.post(`${this.base}/auth/verify-2fa`, payload);
  }
  getUsers(): Observable<any> { return this.http.get(`${this.base}/users`); }
  getLands(): Observable<any> { return this.http.get(`${this.base}/lands`); }
  createLand(payload: any): Observable<any> { return this.http.post(`${this.base}/lands`, payload); }
  getHistory(id: string): Observable<any> { return this.http.get(`${this.base}/lands/${id}/history`); }
  getNotificationsConfig(): Observable<any> { return this.http.get(`${this.base}/notifications/config`); }
  setNotificationsConfig(payload: any): Observable<any> { return this.http.post(`${this.base}/notifications/config`, payload); }
}
