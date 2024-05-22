package hr.fer.eventstore.base.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {
  long countByStreamId(String streamId);
  List<EventJpaEntity> findAllByStreamId(String streamId);
}
