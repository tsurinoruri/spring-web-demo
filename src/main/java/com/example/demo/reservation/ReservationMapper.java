package com.example.demo.reservation;

import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public Reservation toDomainReservation(ReservationEntity it){
        return new Reservation(
                it.getId(),
                it.getUserId(),
                it.getRoomId(),
                it.getStartDate(),
                it.getEndDate(),
                it.getStatus()
        );
    }

    public ReservationEntity toEntityReservation(Reservation it){
        return new ReservationEntity(
                it.id(),
                it.userId(),
                it.roomId(),
                it.startDate(),
                it.endDate(),
                it.status()
        );
    }
}
