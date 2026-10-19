package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Places;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlacesRepository extends JpaRepository<Places, Long> {
}
