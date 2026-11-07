import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/issues';

  stackTrace = '';
  rootCause = '';
  verifiedFix = '';

  aiAnalysis = '';
  hasMemory = false;
  loading = false;
  retainedStatus = false;

  onAnalyze() {
    if (!this.stackTrace.trim()) return;
    this.loading = true;
    this.aiAnalysis = '';

    const payload: any = { stack_trace: this.stackTrace };

    (this.http.post(this.baseUrl + '/analyze', payload) as any).subscribe({
      next: (res: any) => {
        this.aiAnalysis = res.analysis;
        this.hasMemory = res.has_memory;
        this.loading = false;
      },
      error: (err: any) => {
        console.error(err);
        this.aiAnalysis = 'Error connecting to backend server on port 8080.';
        this.loading = false;
      }
    });
  }

  onSubmitFix() {
    if (!this.verifiedFix.trim()) return;

    const payload: any = {
      stack_trace: this.stackTrace,
      root_cause: this.rootCause,
      verified_fix: this.verifiedFix
    };

    (this.http.post(this.baseUrl + '/feedback', payload) as any).subscribe({
      next: () => {
        this.retainedStatus = true;
        setTimeout(() => this.retainedStatus = false, 4000);
      },
      error: (err: any) => console.error(err)
    });
  }
}
export { AppComponent as App };