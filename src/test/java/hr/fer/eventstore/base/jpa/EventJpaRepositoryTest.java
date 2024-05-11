package hr.fer.eventstore.base.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import io.hypersistence.tsid.TSID.Factory;

@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventJpaRepositoryTest extends TestContainersDbFixture {

  @Autowired
  private EventJpaRepository repo;

  @Test
  void saveAndLoad() {
    EventJpaEntity event = new EventJpaEntity();
    long id = Factory.getTsid().toLong();
    event.setId(id);
    event.setData("\"test json\"");
    Map<String, String> meta = Map.of(
        "created", LocalDateTime.now().toString(),
        "ip", "127.0.0.1",
        "userId", "mario");
    event.setMeta(meta);
    event.setEventType("string");
    event.setEventTypeVersion(1);
    event.setStreamId("user-login");
    event.setVersion(1);

    repo.saveAndFlush(event);

    var loadedEvent = repo.findById(event.getId()).get();
    assertThat(loadedEvent.getData()).isEqualTo("\"test json\"");
    assertThat(loadedEvent.getMeta()).isEqualTo(meta);
    assertThat(loadedEvent.getStreamId()).isEqualTo("user-login");
    assertThat(loadedEvent.getVersion()).isEqualTo(1);
    assertThat(loadedEvent.getEventType()).isEqualTo("string");
    assertThat(loadedEvent.getEventTypeVersion()).isEqualTo(1);
  }

}
