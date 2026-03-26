package com.cellc.cartservice.service;

import com.cellc.cartservice.client.ProductClient;
import com.cellc.cartservice.entity.Cart;
import com.cellc.cartservice.entity.CartItem;
import com.cellc.cartservice.repository.CartItemRepository;
import com.cellc.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public Cart getCartOrThrow(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart no encontrado para el usuario"));
    }

    @Transactional
    public void addOrUpdateItem(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity debe ser > 0");
        }

        Cart cart = getOrCreateCart(userId);

        ProductClient.ProductPrice product = productClient.getProductPrice(productId);
        Optional<CartItem> existing = cartItemRepository.findByCartAndProductId(cart, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQuantity = item.getQuantity() + quantity;
            if (product.stock() != null && product.stock() < newQuantity) {
                throw new IllegalArgumentException("Stock insuficiente para el producto");
            }

            item.setQuantity(newQuantity);
            item.setUnitPrice(product.precio());
            cartItemRepository.save(item);
            return;
        }

        if (product.stock() != null && product.stock() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente para el producto");
        }

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(product.precio());

        cartItemRepository.save(item);
    }

    @Transactional
    public void setItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = getCartOrThrow(userId);
        if (quantity == null || quantity <= 0) {
            // En la práctica, en UI normalmente "0" equivale a eliminar.
            cartItemRepository.deleteByCartAndProductId(cart, productId);
            return;
        }

        ProductClient.ProductPrice product = productClient.getProductPrice(productId);
        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProductId(productId);
                    return newItem;
                });

        item.setQuantity(quantity);
        item.setUnitPrice(product.precio());
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        Cart cart = getCartOrThrow(userId);
        cartItemRepository.deleteByCartAndProductId(cart, productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return;
        }
        cartRepository.delete(cartOpt.get());
    }

    public CartResponse toResponse(Cart cart) {
        List<CartItemDto> items = new ArrayList<>();
        double total = 0.0;
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                double lineTotal = item.getUnitPrice() * item.getQuantity();
                total += lineTotal;
                items.add(new CartItemDto(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        lineTotal
                ));
            }
        }
        return new CartResponse(cart.getId(), cart.getUserId(), items, total);
    }

    public record CartResponse(Long cartId, Long userId, List<CartItemDto> items, Double total) {}
    public record CartItemDto(Long productId, Integer quantity, Double unitPrice, Double lineTotal) {}
}

