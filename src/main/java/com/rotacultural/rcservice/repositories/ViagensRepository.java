package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Viagens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViagensRepository extends JpaRepository<Viagens,Long> {
}
