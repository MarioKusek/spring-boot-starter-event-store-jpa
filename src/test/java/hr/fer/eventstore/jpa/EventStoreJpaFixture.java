package hr.fer.eventstore.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import hr.fer.eventstore.base.EventMapper;
import hr.fer.eventstore.base.EventStore;
import hr.fer.icecream_truck.events.TruckEventData;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class EventStoreJpaFixture extends TestContainersDbFixture {

  @Autowired
  private EventRepository repo;

  protected EventStore<TruckEventData> createStore(EventMapper<TruckEventData> mapper) {
    return new EventStoreDB<>(repo, mapper);
  }
}