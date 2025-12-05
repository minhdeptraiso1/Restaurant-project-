package com.hoabanrestaurant.backendchat.dto;


import java.time.Instant;

public record BookingCommand(
        Instant startTime,
        Instant endTime,
        int partySize,
        String note
) { }