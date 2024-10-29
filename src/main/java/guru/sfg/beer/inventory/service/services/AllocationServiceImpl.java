package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.brewery.model.BeerOrderDto;
import guru.sfg.beer.brewery.model.BeerOrderLineDto;
import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating orderId: {}", beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLine -> {
            if ((((beerOrderLine.getOrderQuantity() != null ? beerOrderLine.getOrderQuantity() : 0)
                - (beerOrderLine.getQuantityAllocated() != null ? beerOrderLine.getQuantityAllocated() : 0)) > 0)) {
                allocateBeerOrderLine(beerOrderLine);
            }
            totalOrdered.set(totalOrdered.get() + beerOrderLine.getOrderQuantity());
            totalAllocated.set(totalAllocated.get() + (beerOrderLine.getQuantityAllocated() != null ? beerOrderLine.getQuantityAllocated() : 0));
        });

        log.debug("Total Ordered: {} Total Allocated: {}", beerOrderDto.getId(), totalOrdered.get());

        return totalOrdered.get() == totalAllocated.get();
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLineDto) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLineDto.getUpc());
        beerInventoryList.forEach(beerInventory -> {
            int inventory = (beerInventory.getQuantityOnHand() == null) ? 0 : beerInventory.getQuantityOnHand();
            int orderQty = (beerOrderLineDto.getOrderQuantity() == null) ? 0 : beerOrderLineDto.getOrderQuantity();
            int allocatedQuantity = (beerOrderLineDto.getQuantityAllocated() == null) ? 0 : beerOrderLineDto.getQuantityAllocated();
            int qtyToAllocate = orderQty - allocatedQuantity;

            if(inventory >= qtyToAllocate) {
                inventory -= qtyToAllocate;
                beerOrderLineDto.setQuantityAllocated(orderQty);
                beerInventory.setQuantityOnHand(inventory);

                beerInventoryRepository.save(beerInventory);
            } else if(inventory > 0){
                beerOrderLineDto.setQuantityAllocated(allocatedQuantity + inventory);
                beerInventory.setQuantityOnHand(0);

                beerInventoryRepository.delete(beerInventory);
            }
        });
    }
}
