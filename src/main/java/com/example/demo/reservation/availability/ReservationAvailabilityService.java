package com.example.demo.reservation.availability;

import com.example.demo.reservation.ReservationRepository;
import com.example.demo.reservation.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {

    private final ReservationRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    public ReservationAvailabilityService(ReservationRepository repository) {
        this.repository = repository;
    }

    public boolean isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ) throws IllegalAccessException {
        if(!endDate.isAfter(startDate)){
            throw new IllegalAccessException("Start date be must be one day early than end date");
        }
        List<Long> conflictingIds = repository.findConflictReservationIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );
        if(conflictingIds.isEmpty()){
            return true;
        }
        logger.info("Conflict with ids={}",conflictingIds);
        return false;
    }
}
