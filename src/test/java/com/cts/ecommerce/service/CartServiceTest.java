package com.cts.ecommerce.service;

import com.cts.ecommerce.dto.CartResponse;
import com.cts.ecommerce.mapper.CartMapper;
import com.cts.ecommerce.model.Cart;
import com.cts.ecommerce.model.CartItem;
import com.cts.ecommerce.model.Product;
import com.cts.ecommerce.model.User;
import com.cts.ecommerce.repository.CartItemRepository;
import com.cts.ecommerce.repository.CartRepository;
import com.cts.ecommerce.repository.ProductRepository;
import com.cts.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private CartRepository cartRepository;
	@Mock
	private CartItemRepository cartItemRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private CartMapper cartMapper;

	@InjectMocks
	private CartService cartService;

	private User user;
	private Cart cart;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setEmail("john@example.com");

		cart = new Cart();
		cart.setId(10L);
		cart.setUser(user);
		cart.setItems(new ArrayList<>());
	}

	@Test
	void getCart_createsNewCartIfMissing() {
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
		when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
			Cart saved = inv.getArgument(0);
			saved.setId(99L);
			return saved;
		});

		CartResponse dto = new CartResponse(99L, List.of(), 0.0);
		when(cartMapper.toDto(any(Cart.class))).thenReturn(dto);

		CartResponse result = cartService.getCart(user.getEmail());

		assertThat(result.getCartId()).isEqualTo(99L);
		verify(cartRepository).save(any(Cart.class));
	}

	@Test
	void addToCart_addsNewItemOrIncrements() {
		Product product = new Product();
		product.setId(5L);
		product.setPrice(100.0);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
		when(productRepository.findById(5L)).thenReturn(Optional.of(product));
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);
		when(cartMapper.toDto(cart)).thenReturn(new CartResponse(10L, List.of(), 0.0));

		CartResponse result = cartService.addToCart(user.getEmail(), 5L, 2);

		assertThat(cart.getItems()).hasSize(1);
		assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
		assertThat(result.getCartId()).isEqualTo(10L);

		// call again to increment
		cart.getItems().get(0).setProduct(product);
		CartResponse result2 = cartService.addToCart(user.getEmail(), 5L, 3);
		assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
		assertThat(result2.getCartId()).isEqualTo(10L);
	}

	@Test
	void removeFromCart_removesProduct() {
		Product p1 = new Product(); p1.setId(1L);
		Product p2 = new Product(); p2.setId(2L);

		CartItem i1 = new CartItem(); i1.setProduct(p1); i1.setCart(cart); i1.setQuantity(1);
		CartItem i2 = new CartItem(); i2.setProduct(p2); i2.setCart(cart); i2.setQuantity(1);
		cart.getItems().add(i1);
		cart.getItems().add(i2);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
		when(cartRepository.save(cart)).thenReturn(cart);
		when(cartMapper.toDto(cart)).thenReturn(new CartResponse(10L, List.of(), 0.0));

		CartResponse res = cartService.removeFromCart(user.getEmail(), 1L);
		assertThat(cart.getItems()).hasSize(1);
		assertThat(cart.getItems().get(0).getProduct().getId()).isEqualTo(2L);
		assertThat(res.getCartId()).isEqualTo(10L);
	}

	@Test
	void clearCart_emptiesItems() {
		CartItem i = new CartItem(); i.setCart(cart); i.setQuantity(2); i.setProduct(new Product());
		cart.getItems().add(i);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
		when(cartRepository.save(cart)).thenReturn(cart);
		when(cartMapper.toDto(cart)).thenReturn(new CartResponse(10L, List.of(), 0.0));

		CartResponse res = cartService.clearCart(user.getEmail());
		assertThat(cart.getItems()).isEmpty();
		assertThat(res.getCartId()).isEqualTo(10L);
	}

	@Test
	void increaseQuantity_incrementsItem() {
		CartItem item = new CartItem();
		item.setId(33L);
		item.setCart(cart);
		item.setQuantity(1);

		when(cartItemRepository.findById(33L)).thenReturn(Optional.of(item));
		when(cartMapper.toDto(cart)).thenReturn(new CartResponse(10L, List.of(), 0.0));

		CartResponse res = cartService.increaseQuantity(33L);
		assertThat(item.getQuantity()).isEqualTo(2);
		verify(cartItemRepository).save(item);
		assertThat(res.getCartId()).isEqualTo(10L);
	}

	@Test
	void decreaseQuantity_deletesWhenZero() {
		CartItem item = new CartItem();
		item.setId(44L);
		item.setCart(cart);
		item.setQuantity(1);

		when(cartItemRepository.findById(44L)).thenReturn(Optional.of(item));
		when(cartRepository.save(cart)).thenReturn(cart);
		when(cartMapper.toDto(cart)).thenReturn(new CartResponse(10L, List.of(), 0.0));

		CartResponse res = cartService.decreaseQuantity(44L);
		verify(cartItemRepository).delete(item);
		assertThat(res.getCartId()).isEqualTo(10L);
	}

	@Test
	void decreaseQuantity_decrementsWhenAboveOne() {
		CartItem item = new CartItem();
		item.setId(55L);
		item.setCart(cart);
		item.setQuantity(3);

		when(cartItemRepository.findById(55L)).thenReturn(Optional.of(item));
		when(cartMapper.toDto(cart)).thenReturn(new CartResponse(10L, List.of(), 0.0));
		when(cartRepository.save(cart)).thenReturn(cart);

		CartResponse res = cartService.decreaseQuantity(55L);
		assertThat(item.getQuantity()).isEqualTo(2);
		verify(cartItemRepository).save(item);
		assertThat(res.getCartId()).isEqualTo(10L);
	}
}
