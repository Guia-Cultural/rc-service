package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Favorites;
import com.rotacultural.rcservice.entities.FavoritesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, FavoritesId> {
}
