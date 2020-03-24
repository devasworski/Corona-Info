import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { CoronadataService } from '../coronadata.service';

@Component({
  selector: 'app-landkreis-detail',
  templateUrl: './landkreis-detail.component.html',
  styleUrls: ['./landkreis-detail.component.scss']
})
export class LandkreisDetailComponent implements OnInit, OnDestroy {

  private paramKey = 'objectId';

  public data$: any;

  private paramSubscription: Subscription;
  public objectId: string;

  constructor(private route: ActivatedRoute, private readonly coronaService: CoronadataService) { }

  ngOnInit(): void {

    this.paramSubscription = this.route.params.subscribe(params => {
      this.objectId = params[this.paramKey];
    });
    this.data$ = this.coronaService.getLandkreisData(this.objectId);
  }

  ngOnDestroy(): void {
    this.paramSubscription.unsubscribe();
  }

}
