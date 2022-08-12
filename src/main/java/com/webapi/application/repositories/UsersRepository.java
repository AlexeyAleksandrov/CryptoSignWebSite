package com.webapi.application.repositories;

import com.webapi.application.models.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long>
{
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
