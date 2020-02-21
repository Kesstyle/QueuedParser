package by.kes.queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class QueuedParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueuedParserApplication.class, args);
	}

}
