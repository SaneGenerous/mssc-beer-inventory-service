package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.brewery.model.BeerOrderDto;

public interface AllocationService {
    Boolean allocateOrder(BeerOrderDto beerOrderDto);
}
