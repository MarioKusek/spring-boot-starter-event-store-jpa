package hr.fer.eventstore.jpa;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;

@AutoConfiguration(after = {JpaRepositoriesAutoConfiguration.class})
@ConditionalOnBean(DataSource.class)
@ConditionalOnClass(JpaRepository.class)
@Import({StarterEntityRegistrar.class})
public class EventStoreAutoConfiguration {

}
