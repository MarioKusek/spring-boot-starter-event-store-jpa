package hr.fer.event.store.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManager;

@Configuration(proxyBeanMethods = false)
public class EventStoreConfiguration {
  @Bean
  EventRepository eventRepository(EntityManager em) {
    return new EventRepositoryWithEntityManager(em);
  }


}
