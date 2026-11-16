package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    List<Itinerary> findByTrip_IdTripOrderByDayAscIdItinerarioAsc(Long tripId);

    List<Itinerary> findByTrip_User_IdUserOrderByDayAscIdItinerarioAsc(Long userId);

    long countByTrip_IdTrip(Long tripId);

    void deleteByTrip_IdTrip(Long tripId);

    void deleteByLocale_IdLoc(Long locId);
}
