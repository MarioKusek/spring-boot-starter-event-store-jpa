package hr.fer.eventstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventSourcingSpringDataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventSourcingSpringDataJpaApplication.class, args);
	}

}
