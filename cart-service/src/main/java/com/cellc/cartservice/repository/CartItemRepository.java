package com.cellc.cartservice.repository;

import com.cellc.cartservice.entity.Cart;
import com.cellc.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);

    void deleteByCartAndProductId(Cart cart, Long productId);
}

