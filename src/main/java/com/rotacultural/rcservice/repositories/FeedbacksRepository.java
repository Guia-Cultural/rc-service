package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Feedbacks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbacksRepository extends JpaRepository<Feedbacks, Long> {
}
