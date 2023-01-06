package hexlet.code.javaproject73.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    @GetMapping("/")
    public String root() {
        return "Welcome to Spring";
    }
}
