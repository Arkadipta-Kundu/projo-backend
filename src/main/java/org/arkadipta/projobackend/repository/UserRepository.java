package org.arkadipta.projobackend.repository;

import org.arkadipta.projobackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByRememberToken(String rememberToken);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
