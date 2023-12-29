package com.antarctica.explorer.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
  @GetMapping("/")
  public String welcome() {
    return "Welcome to Antarctica Explorer!";
  }
}
