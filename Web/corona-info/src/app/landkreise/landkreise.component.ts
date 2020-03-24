import { Component, OnInit } from '@angular/core';
import { CoronadataService } from '../coronadata.service';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-landkreise',
  templateUrl: './landkreise.component.html',
  styleUrls: ['./landkreise.component.scss']
})
export class LandkreiseComponent implements OnInit {

  public landkreise$: any;

  constructor(private readonly coronaService: CoronadataService) { }

  ngOnInit(): void {
    this.landkreise$ = this.coronaService.getLandkreise();
  }

}
