package com.example.demo.reservation.availability;

public record CheckAvailabilityResponse(
            String message,
            AvailabilityStatus status
) {}
