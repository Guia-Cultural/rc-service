package com.rotacultural.rcservice.repositories;


import com.rotacultural.rcservice.entities.Preferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferencesRepository extends JpaRepository<Preferences, Long> {
    boolean existsByTrips_IdTripAndCategories_IdCat(Long tripId, Long catId);
}
