package com.cts.ecommerce.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.ecommerce.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  
  // User status queries
  List<User> findByActiveTrue();
  List<User> findByActiveFalse();
  Optional<User> findByEmailAndActiveTrue(String email);
  Optional<User> findByUsernameAndActiveTrue(String username);
}
