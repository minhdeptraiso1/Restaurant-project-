package com.hoabanrestaurant.backendchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@SpringBootApplication
public class BackendchatApplication {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static void main(String[] args) {
        System.out.println(ZonedDateTime.now());
        System.out.println(ZonedDateTime.now(VN_ZONE));
        System.out.println(LocalDate.now(VN_ZONE));
        System.out.println(Instant.now());
        SpringApplication.run(BackendchatApplication.class, args);
    }

}
