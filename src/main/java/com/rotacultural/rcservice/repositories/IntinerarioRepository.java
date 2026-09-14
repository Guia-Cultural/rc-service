package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Intinerario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntinerarioRepository extends JpaRepository<Intinerario, Long> {
}
