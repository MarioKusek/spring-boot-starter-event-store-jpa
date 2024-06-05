package hr.fer.event.store.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;

@Configuration
@ConditionalOnClass(EntityManager.class)
@Import({EventStoreEntityRegistrar.class, EventRepositoryWithEntityManager.class})
public class EventStoreAutoConfiguration {
}
