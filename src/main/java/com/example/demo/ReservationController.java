package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/reservation")
@RestController//обработчик http запросов
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id
            ) throws NoSuchFieldException {
        logger.info("get reservation by id called " + id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(id));
        //return reservationService.getReservationById(id);
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservation(){
        logger.info("All reservations called");
        return ResponseEntity.ok(reservationService.findAllReservations());
        //return reservationService.findAllReservations();
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservationToCreate) throws IllegalAccessException {
        logger.info("Post caleed");
        try{
            return ResponseEntity.status(201)
                    .body(reservationService.createReservation(reservationToCreate));

        }catch (Exception e){
            return ResponseEntity.status(404).build();
        }
       // return reservationService.createReservation(reservationToCreate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody Reservation reservationToupdate
            ) throws NoSuchFieldException {
        logger.info("Update called " + id);
        var updated = reservationService.updateReservation(id, reservationToupdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) throws NoSuchFieldException {
        logger.info("delete called " + id);
        try{
            reservationService.cancelReservation(id);
            return ResponseEntity.ok().build();
        }catch (NoSuchFieldException e){
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ) throws NoSuchFieldException {
        logger.info("Approve called " + id);
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }
}
