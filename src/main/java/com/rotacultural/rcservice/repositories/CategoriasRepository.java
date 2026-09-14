package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Categorias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriasRepository extends JpaRepository<Categorias, Long> {
}
