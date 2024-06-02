package hr.fer.event.store.jpa;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;

@AutoConfiguration(after = {JpaRepositoriesAutoConfiguration.class})
@ConditionalOnBean(DataSource.class)
@ConditionalOnClass(JpaRepository.class)
@Import({EventStoreConfiguration.class})
@EntityScan(basePackageClasses =  {EventStoreAutoConfiguration.class})
public class EventStoreAutoConfiguration {

}
