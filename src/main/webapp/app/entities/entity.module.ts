import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
    imports: [
        RouterModule.forChild([
            {
                path: 'region',
                loadChildren: './region/region.module#MoboRegionModule'
            },
            {
                path: 'country',
                loadChildren: './country/country.module#MoboCountryModule'
            },
            {
                path: 'location',
                loadChildren: './location/location.module#MoboLocationModule'
            },
            {
                path: 'department',
                loadChildren: './department/department.module#MoboDepartmentModule'
            },
            {
                path: 'task',
                loadChildren: './task/task.module#MoboTaskModule'
            },
            {
                path: 'employee',
                loadChildren: './employee/employee.module#MoboEmployeeModule'
            },
            {
                path: 'job',
                loadChildren: './job/job.module#MoboJobModule'
            },
            {
                path: 'job-history',
                loadChildren: './job-history/job-history.module#MoboJobHistoryModule'
            }
            /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
        ])
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class MoboEntityModule {}
