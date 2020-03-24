import { TestBed } from '@angular/core/testing';

import { CoronadataService } from './coronadata.service';

describe('CoronadataService', () => {
  let service: CoronadataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CoronadataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
