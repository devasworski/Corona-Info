import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LandkreiseComponent } from './landkreise/landkreise.component';
import { LandkreisDetailComponent } from './landkreis-detail/landkreis-detail.component';


const routes: Routes = [
  {
    path: '', pathMatch: 'full', redirectTo: 'landkreise'
  },
  {
    path: 'landkreise', component: LandkreiseComponent
  },
  {
    path: 'landkreise/:objectId', component: LandkreisDetailComponent
  },
  {
    path: '**', redirectTo: 'landkreise'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    enableTracing: true
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
