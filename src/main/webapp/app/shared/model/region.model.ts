export interface IRegion {
    id?: number;
    categorie1?: string;
}

export class Region implements IRegion {
    constructor(public id?: number, public categorie1?: string) {}
}
