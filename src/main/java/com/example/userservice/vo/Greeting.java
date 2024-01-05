package com.example.userservice.vo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "greeting")
@Data
@Component
public class Greeting {
    private String message;
}
