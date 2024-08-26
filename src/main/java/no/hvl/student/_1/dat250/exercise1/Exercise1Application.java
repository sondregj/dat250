package no.hvl.student._1.dat250.exercise1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Exercise1Application {

    public static void main(String[] args) {
        SpringApplication.run(Exercise1Application.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "<h1>Exercise 1</h1>";
    }
}
