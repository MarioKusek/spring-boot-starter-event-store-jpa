package hr.fer.icecream_truck;

import org.springframework.stereotype.Component;

import hr.fer.eventstore.base.jpa.EventJpaRepository;
import jakarta.annotation.PostConstruct;

@Component
public class Main {
  private EventJpaRepository repo;

  public Main(EventJpaRepository repo) {
    this.repo = repo;
  }

  @PostConstruct
  void run() {
    //basicExamples();

    evolveExample();
  }

  private void evolveExample() {
    // TODO uncomment
//    EventStore<TruckEventData> store = new EventStoreDB<>(repo, Map.of());
//
//    store.append(new FlavourRestocked("vanilija", 1));
//
//    System.out.println(new StockState().fold(store.get()));
//    System.out.println(store.get());
//
//    store.evolve(new SoldCommand("vanilija"));
//    System.out.println(new StockState().fold(store.get()));
//    System.out.println(store.get());

  }

  // TODO uncomment
  private void basicExamples() {
//    EventStore<TruckEventData> store = new EventStore<>();
//
//    store.append(new FlavourRestocked("vanilija", 1));
//    store.append(new FlavourSold("vanilija"));
//    store.append(new FlavourSold("vanilija"));
//    store.append(new FlavourSold("jagoda"));
//
//    System.out.println(new SoldOneFlavour("vanilija").fold(store.get()));
//
//    System.out.println(new SoldFlavours().fold(store.get()));
//
//    System.out.println(new StockState().fold(store.get()));
//
//    System.out.println(store.get());
  }

}
