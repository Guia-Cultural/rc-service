package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlacesRepository extends JpaRepository<Places, Long> {
    List<Places> findByStatusOrderByDateCreateDesc(Status status);

    List<Places> findByCreateBy_IdUserOrderByDateCreateDesc(Long userId);

    List<Places> findAllByOrderByDateCreateDesc();

    boolean existsByNameIgnoreCaseAndCityIgnoreCase(String name, String city);

    List<Places> findByCategory_IdCat(Long catId);
}
