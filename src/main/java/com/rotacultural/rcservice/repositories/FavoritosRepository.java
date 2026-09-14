package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Favoritos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoritosRepository extends JpaRepository<Favoritos, Long> {
}
