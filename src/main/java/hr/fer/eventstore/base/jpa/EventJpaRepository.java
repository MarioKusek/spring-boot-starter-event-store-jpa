package hr.fer.eventstore.base.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {
  long countByStreamId(String streamId);
}
