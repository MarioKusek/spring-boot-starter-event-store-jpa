package hr.fer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("hr.fer")
public class EventSourcingSpringDataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventSourcingSpringDataJpaApplication.class, args);
	}

}
