package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Itinerario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItinerarioRepository extends JpaRepository<Itinerario, Long> {
}
