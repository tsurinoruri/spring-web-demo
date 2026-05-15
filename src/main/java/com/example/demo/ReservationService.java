package com.example.demo;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {

    private ReservationRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }

    public Reservation getReservationById(Long id) throws NoSuchFieldException {
        //Optional<ReservationEntity> find =  repository.findById(id);
        ReservationEntity findByIdentity =  repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("not found reservation " + id));
        return toDomainReservation(findByIdentity);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntityes =  repository.findAll();
        List<Reservation> allList = allEntityes.stream()
                .map(this::toDomainReservation)
                .toList();
        return allList;
    }

    public Reservation createReservation(Reservation reservationToCreate) throws IllegalAccessException {
        if(reservationToCreate.id() != null){
            throw new IllegalAccessException("Id shoul be reality");
        }
        if(reservationToCreate.status() != null){
            throw new IllegalAccessException("Status should be empty");
        }
        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );
        var savedEntity = repository.save(entityToSave);
        return toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) throws NoSuchFieldException {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new NoSuchFieldException("Not found " + id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("could not modify " + id + " status=" + reservationEntity.getStatus());
        }

        var updatedReservation = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        //reservationMap.put(reservationEntity.getId(), reservationToUpdate);
        var updatedEntity = repository.save(updatedReservation);
        return toDomainReservation(updatedEntity) ;
    }

    @Transactional
    public void cancelReservation(Long id) throws NoSuchFieldException {
        if(!repository.existsById(id)){
            throw new NoSuchFieldException("Not found reservation for delete");
        }
        logger.info("Cancel successfully " + id);
        repository.setStatus(id, ReservationStatus.CANCELLED);
    }

    public Reservation approveReservation(Long id) throws NoSuchFieldException {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new NoSuchFieldException("Not found " + id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("could not approved " + id + " status=" + reservationEntity.getStatus());
        }
        var isConflict = isReservationConflict(reservationEntity);
        if(isConflict){
            throw new IllegalStateException("could not approved because of conflict " + id + " status=" + reservationEntity.getStatus());
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);
        return toDomainReservation(reservationEntity);
    }

    private boolean isReservationConflict(ReservationEntity reservation){

        var allReservation = repository.findAll();
        for(ReservationEntity existingReservation : allReservation){
            if(reservation.getId().equals(existingReservation.getId())){
                continue;
            }
            if(!reservation.getRoomId().equals(existingReservation.getRoomId())){
                continue;
            }
            if(!existingReservation.getStatus().equals(ReservationStatus.APPROVED)){
                continue;
            }
            if(reservation.getStartDate().isBefore(existingReservation.getEndDate())
                && existingReservation.getStartDate().isBefore(reservation.getEndDate())){
                return true;
            }
        }
        return false;
    }

    private Reservation toDomainReservation(ReservationEntity it){
        return new Reservation(
                it.getId(),
                it.getUserId(),
                it.getRoomId(),
                it.getStartDate(),
                it.getEndDate(),
                it.getStatus()
        );
    }
}
