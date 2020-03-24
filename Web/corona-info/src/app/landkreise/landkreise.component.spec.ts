import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LandkreiseComponent } from './landkreise.component';

describe('LandkreiseComponent', () => {
  let component: LandkreiseComponent;
  let fixture: ComponentFixture<LandkreiseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LandkreiseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LandkreiseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
