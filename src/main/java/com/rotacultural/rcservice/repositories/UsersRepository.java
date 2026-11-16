package com.rotacultural.rcservice.repositories;

import com.rotacultural.rcservice.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    List<Users> findByCep(String cep);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailAndIdUserNot(String email, Long idUser);

    boolean existsByEmailIgnoreCaseAndIdUserNot(String email, Long idUser);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByEmailIgnoreCase(String email);
}
