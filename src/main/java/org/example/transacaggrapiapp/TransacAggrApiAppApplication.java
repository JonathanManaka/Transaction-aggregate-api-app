package org.example.transacaggrapiapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransacAggrApiAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransacAggrApiAppApplication.class, args);
    }

}
