package com.example.demo.reservation;

import com.example.demo.reservation.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {

    private ReservationRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationMapper reservationMapper;
    private final ReservationAvailabilityService availabilityService;

    public ReservationService(ReservationRepository repository, ReservationMapper reservationMapper, ReservationAvailabilityService availabilityService) {
        this.reservationMapper = reservationMapper;
        this.repository = repository;
        this.availabilityService = availabilityService;
    }

    public Reservation getReservationById(Long id) throws NoSuchFieldException {
        //Optional<ReservationEntity> find =  repository.findById(id);
        ReservationEntity findByIdentity =  repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("not found reservation " + id));
        return reservationMapper.toDomainReservation(findByIdentity);
    }

    public List<Reservation> findAllByFilter(
            ReservationSearchFilter filter
    ) {

        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<ReservationEntity> allEntityes =  repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );
        List<Reservation> allList = allEntityes.stream()
                .map(reservationMapper::toDomainReservation)
                .toList();
        return allList;
    }

    public Reservation createReservation(Reservation reservationToCreate) throws IllegalAccessException {
        if(reservationToCreate.status() != null){
            throw new IllegalArgumentException("Status should be empty");
        }
        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())){
            throw new IllegalAccessException("Start date be must be one day early than end date");
        }
        var entityToSave = reservationMapper.toEntityReservation(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);
        var savedEntity = repository.save(entityToSave);
        return reservationMapper.toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) throws NoSuchFieldException, IllegalAccessException, IllegalStateException {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new NoSuchFieldException("Not found " + id));

        if(!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())){
            throw new IllegalAccessException("Start date be must be one day early than end date");
        }

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Could not modify " + id + " status=" + reservationEntity.getStatus());
        }

        var updatedReservation = reservationMapper.toEntityReservation(reservationToUpdate);
        updatedReservation.setStatus(ReservationStatus.PENDING);
        updatedReservation.setId(reservationEntity.getId());
        //reservationMap.put(reservationEntity.getId(), reservationToUpdate);
        var updatedEntity = repository.save(updatedReservation);
        return reservationMapper.toDomainReservation(updatedEntity) ;
    }

    @Transactional
    public void cancelReservation(Long id) throws NoSuchFieldException {
        var reservation = repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id " + id));

        if(reservation.getStatus().equals(ReservationStatus.APPROVED)){
            throw new IllegalStateException("Cannot cancel approved reservation by " + id + ", please, contact with menager");
        }

        if(reservation.getStatus().equals(ReservationStatus.CANCELLED)){
            throw new IllegalStateException("Cannot cancel reservation, because it already cancelled");
        }

        logger.info("Cancel successfully " + id);
        repository.setStatus(id, ReservationStatus.CANCELLED);
    }

    public Reservation approveReservation(Long id) throws NoSuchFieldException, IllegalAccessException {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new NoSuchFieldException("Not found " + id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("could not approved " + id + " status=" + reservationEntity.getStatus());
        }
        var isAvailable = availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );
        if(!isAvailable){
            throw new IllegalStateException("could not approved because of conflict " + id + " status=" + reservationEntity.getStatus());
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);
        return reservationMapper.toDomainReservation(reservationEntity);
    }


}
