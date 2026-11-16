package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Trips;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripsRepository extends JpaRepository<Trips, Long> {
    List<Trips> findByUser_IdUserOrderByDateCreateDesc(Long userId);

    void deleteByUser_IdUser(Long userId);
}
