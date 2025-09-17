package com.cts.ecommerce.service;

import com.cts.ecommerce.model.Role;
import com.cts.ecommerce.model.User;
import com.cts.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl service;

	@Test
	void loadUserByUsername_returnsUserDetails() {
		User u = new User();
		u.setEmail("john@example.com");
		u.setPassword("encoded");
		u.setRoles(Set.of(Role.ROLE_USER));

		when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(u));

		UserDetails details = service.loadUserByUsername("john@example.com");
		assertThat(details.getUsername()).isEqualTo("john@example.com");
		assertThat(details.getPassword()).isEqualTo("encoded");
		assertThat(details.getAuthorities()).extracting("authority").contains("ROLE_USER");
	}

	@Test
	void loadUserByUsername_throwsWhenMissing() {
		when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
		assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@example.com"));
	}
}
