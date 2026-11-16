package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.PlaceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {
    List<PlaceImage> findByPlace_IdLocOrderByCoverDescDateCreateAsc(Long idLoc);

    long countByPlace_IdLoc(Long idLoc);

    void deleteByPlace_IdLoc(Long idLoc);
}
