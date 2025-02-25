package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import guru.sfg.beer.brewery.model.events.NewInventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewInventoryListener {

    private final BeerInventoryRepository beerInventoryRepository;

    @JmsListener(destination = JmsConfig.NEW_INVENTORY_QUEUE)
    public void listener(NewInventoryEvent event){
        log.debug("Got Inventory: {}", event.toString());

        beerInventoryRepository.save(BeerInventory.builder()
                        .beerId(event.getBeerDTO().getId())
                        .upc(event.getBeerDTO().getUpc())
                        .quantityOnHand(event.getBeerDTO().getQuantityOnHand())
                        .build());
    }
}
