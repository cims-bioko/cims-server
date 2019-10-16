package com.github.cimsbioko.server;

import com.github.cimsbioko.server.scripting.ContextFactoryRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.addListeners(new ContextFactoryRegistrar());
        app.run(args);
    }
}