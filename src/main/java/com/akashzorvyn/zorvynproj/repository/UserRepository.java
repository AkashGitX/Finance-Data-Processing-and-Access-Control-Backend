package com.akashzorvyn.zorvynproj.repository;

import com.akashzorvyn.zorvynproj.entity.User;
import com.akashzorvyn.zorvynproj.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);
}
