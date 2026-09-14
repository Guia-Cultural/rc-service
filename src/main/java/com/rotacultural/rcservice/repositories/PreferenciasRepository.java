package com.rotacultural.rcservice.repositories;


import com.rotacultural.rcservice.entities.Preferencias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferenciasRepository extends JpaRepository<Preferencias, Long> {
}
