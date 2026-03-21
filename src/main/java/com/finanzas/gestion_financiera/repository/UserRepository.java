package com.finanzas.gestion_financiera.repository;

import com.finanzas.gestion_financiera.entity.User;
import com.finanzas.gestion_financiera.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
