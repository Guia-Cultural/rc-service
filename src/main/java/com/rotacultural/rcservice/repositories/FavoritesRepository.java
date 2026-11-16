package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Favorites;
import com.rotacultural.rcservice.entities.FavoritesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, FavoritesId> {
    List<Favorites> findByUser_IdUserOrderByDateCreateDesc(Long userId);

    void deleteByUser_IdUser(Long userId);

    void deleteByPlace_IdLoc(Long locId);
}
