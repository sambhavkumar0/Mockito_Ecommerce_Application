package com.cts.ecommerce.service;


import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.cts.ecommerce.model.User;
import com.cts.ecommerce.repository.UserRepository;

import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;
  public UserDetailsServiceImpl(UserRepository userRepository) { this.userRepository = userRepository; }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmailAndActiveTrue(email)
      .orElseThrow(() -> new UsernameNotFoundException("User not found or inactive: " + email));
    
    // Additional check for active status (redundant but safe)
    if (!user.isActive()) {
      throw new UsernameNotFoundException("User account is inactive: " + email);
    }
    
    return new org.springframework.security.core.userdetails.User(
      user.getEmail(), // Use email as the principal
      user.getPassword(),
      user.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).collect(Collectors.toList())
    );
  }
}
