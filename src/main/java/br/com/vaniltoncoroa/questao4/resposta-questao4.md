# Experiência com Angular — recursos centrais, casos de uso e exemplo prático (Angular 5/6)

## Visão geral
Atuei com **Angular 5 e 6** (e AngularJS) na **Seguros Sura**, em sistemas de **cotação e emissão** (Auto/Transporte). Usei:
- Componentes, diretivas e pipes
- Data binding (`{{}}`, `[prop]`, `(event)`, `[(ngModel)]`)
- **Reactive Forms**
- **RxJS/Observables**
- **HttpClient** e **HTTP Interceptors**
- **Route Guards** e **Lazy Loading**
- **ChangeDetectionStrategy.OnPush** e `trackBy` em listas

## Caso prático
No portal de cotação, o Angular consumia microserviços (Java/Spring Boot) para dados de cliente, veículo e coberturas.
- Formulários multi‑etapas com **Reactive Forms** e validação
- **Interceptor** para anexar JWT e padronizar erros
- **Componente filho** emitindo dados (ex.: endereço via CEP) para preencher formulário pai
- **Lazy modules** e `OnPush` para performance

---

## Código — exemplo

### `customer.service.ts`
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'; // Angular 5 com RxJS 5.5

export interface Customer {
  id?: string;
  name: string;
  document: string;
  address?: { street: string; city: string; zip: string };
}

@Injectable() // Angular 5: registrar em providers do módulo
export class CustomerService {
  private baseUrl = '/api/customers';
  constructor(private http: HttpClient) {}

  searchByDocument(document: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.baseUrl}?document=${document}`);
  }

  save(customer: Customer): Observable<Customer> {
    return this.http.post<Customer>(this.baseUrl, customer);
  }
}
```

### `auth.interceptor.ts`
```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('jwt');
    const authReq = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
    return next.handle(authReq);
  }
}
```

### `app.module.ts` (registro de service/interceptor)
```typescript
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { CustomerFormComponent } from './customer-form.component';
import { AddressSearchComponent } from './address-search.component';

import { CustomerService } from './customer.service';
import { AuthInterceptor } from './auth.interceptor';

@NgModule({
  declarations: [AppComponent, CustomerFormComponent, AddressSearchComponent],
  imports: [BrowserModule, HttpClientModule, ReactiveFormsModule, FormsModule],
  providers: [
    CustomerService, // Angular 5: provider explícito
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
```

### `address-search.component.ts` (filho → pai via `EventEmitter`)
```typescript
import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-address-search',
  template: `
    <input [(ngModel)]="zip" placeholder="CEP" />
    <button (click)="search()">Buscar</button>
  `
})
export class AddressSearchComponent {
  @Output() addressSelected = new EventEmitter<{street: string; city: string; zip: string}>();
  zip = '';

  search() {
    // Mock: em produção, chamar serviço de CEP via HttpClient
    const result = { street: 'Av. Paulista, 1000', city: 'São Paulo', zip: this.zip || '01310-100' };
    this.addressSelected.emit(result);
  }
}
```

### `customer-form.component.ts` (pai com Reactive Forms)
```typescript
import { Component, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { CustomerService } from './customer.service';

@Component({
  selector: 'app-customer-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form [formGroup]="form" (ngSubmit)="submit()">
      <label>Documento</label>
      <input formControlName="document" (blur)="prefill()" />

      <label>Nome</label>
      <input formControlName="name" />

      <fieldset formGroupName="address">
        <legend>Endereço</legend>
        <app-address-search (addressSelected)="applyAddress($event)"></app-address-search>
        <input placeholder="Rua" formControlName="street" />
        <input placeholder="Cidade" formControlName="city" />
        <input placeholder="CEP" formControlName="zip" />
      </fieldset>

      <button type="submit" [disabled]="form.invalid || loading">
        {{ loading ? 'Salvando…' : 'Salvar' }}
      </button>
    </form>
  `
})
export class CustomerFormComponent {
  loading = false;

  form = this.fb.group({
    document: ['', Validators.required],
    name: ['', [Validators.required, Validators.minLength(3)]],
    address: this.fb.group({
      street: [''],
      city: [''],
      zip: ['']
    })
  });

  constructor(private fb: FormBuilder, private service: CustomerService) {}

  prefill(): void {
    const doc = (this.form.get('document')!.value || '').toString().trim();
    if (!doc) { return; }
    this.service.searchByDocument(doc).subscribe(found => {
      if (found) {
        this.form.patchValue({
          name: found.name,
          address: found.address || {}
        });
      }
    });
  }

  applyAddress(addr: {street: string; city: string; zip: string}) {
    this.form.get('address')!.patchValue(addr);
  }

  submit(): void {
    if (this.form.invalid) { return; }
    this.loading = true;
    this.service.save(this.form.value).subscribe({
      next: _ => this.loading = false,
      error: _ => this.loading = false
    });
  }
}
```

---
