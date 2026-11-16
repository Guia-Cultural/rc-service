package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Feedbacks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbacksRepository extends JpaRepository<Feedbacks, Long> {
    boolean existsByUser_IdUserAndPlaces_IdLoc(Long userId, Long locId);

    Optional<Feedbacks> findByUser_IdUserAndPlaces_IdLoc(Long userId, Long locId);

    List<Feedbacks> findByPlaces_IdLocOrderByDateCreateDesc(Long locId);

    void deleteByUser_IdUser(Long userId);

    void deleteByPlaces_IdLoc(Long locId);
}
