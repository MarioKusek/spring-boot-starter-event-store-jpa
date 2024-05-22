package hr.fer.eventstore.base.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import hr.fer.eventstore.base.EventMapper;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.base.EventStoreDB;
import hr.fer.icecream_truck.events.TruckEventData;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class EventStoreJpaFixture extends TestContainersDbFixture {

  @Autowired
  private EventJpaRepository repo;

  protected EventStore<TruckEventData> createStore(List<hr.fer.eventstore.base.EventMapper.ClassTriple> types, EventMapper<TruckEventData> mapper) {
    return new EventStoreDB<>(repo, mapper);
  }
}
