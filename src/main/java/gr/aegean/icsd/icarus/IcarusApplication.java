package gr.aegean.icsd.icarus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IcarusApplication {

    public static void main(String[] args) {
        SpringApplication.run(IcarusApplication.class, args);
    }

}
