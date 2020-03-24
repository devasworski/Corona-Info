import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LandkreisDetailComponent } from './landkreis-detail.component';

describe('LandkreisDetailComponent', () => {
  let component: LandkreisDetailComponent;
  let fixture: ComponentFixture<LandkreisDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LandkreisDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LandkreisDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
