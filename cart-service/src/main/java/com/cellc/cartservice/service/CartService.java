package com.cellc.cartservice.service;

import com.cellc.cartservice.client.ProductClient;
import com.cellc.cartservice.client.PromotionClient;
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
    private final PromotionClient promotionClient;

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

    @Transactional
    public void applyPromotionCode(Long userId, String promotionCode) {
        Cart cart = getOrCreateCart(userId);
        double subtotal = calculateSubtotal(cart);
        if (subtotal <= 0) {
            throw new IllegalArgumentException("No puedes aplicar una promocion a un carrito vacio");
        }

        PromotionClient.PromotionValidateResponse promotion = promotionClient.validatePromotion(userId, promotionCode, subtotal);
        if (promotion.active() == null || !promotion.active()) {
            throw new IllegalArgumentException("El codigo de promocion no es valido para esta cuenta o carrito");
        }

        cart.setPromotionCode(promotion.code());
        cartRepository.save(cart);
    }

    @Transactional
    public void removePromotionCode(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.setPromotionCode(null);
        cartRepository.save(cart);
    }

    public CartResponse toResponse(Cart cart) {
        List<CartItemDto> items = new ArrayList<>();
        double subtotal = 0.0;
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                double lineTotal = item.getUnitPrice() * item.getQuantity();
                subtotal += lineTotal;
                items.add(new CartItemDto(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        lineTotal
                ));
            }
        }

        AppliedPromotionDto appliedPromotion = null;
        double discountAmount = 0.0;
        if (cart.getPromotionCode() != null && !cart.getPromotionCode().isBlank()) {
            PromotionClient.PromotionValidateResponse promotion = promotionClient.validatePromotion(
                    cart.getUserId(), cart.getPromotionCode(), subtotal
            );
            if (promotion.active() != null && promotion.active()) {
                discountAmount = calculateDiscount(subtotal, promotion);
                appliedPromotion = new AppliedPromotionDto(
                        promotion.code(),
                        promotion.discountType(),
                        promotion.discountPercent(),
                        promotion.fixedAmount(),
                        promotion.minimumOrderAmount(),
                        promotion.usageLimit(),
                        promotion.timesUsed(),
                        promotion.usedByUser()
                );
            } else {
                cart.setPromotionCode(null);
                cartRepository.save(cart);
            }
        }

        return new CartResponse(cart.getId(), cart.getUserId(), items, cart.getPromotionCode(), appliedPromotion, subtotal, discountAmount, subtotal - discountAmount);
    }

    private double calculateSubtotal(Cart cart) {
        double subtotal = 0.0;
        if (cart.getItems() == null) {
            return subtotal;
        }
        for (CartItem item : cart.getItems()) {
            subtotal += item.getUnitPrice() * item.getQuantity();
        }
        return subtotal;
    }

    private double calculateDiscount(double subtotal, PromotionClient.PromotionValidateResponse promotion) {
        if (promotion == null || promotion.active() == null || !promotion.active()) {
            return 0.0;
        }
        if ("FIXED_AMOUNT".equalsIgnoreCase(promotion.discountType())) {
            double fixedAmount = promotion.fixedAmount() == null ? 0.0 : promotion.fixedAmount();
            return Math.min(subtotal, fixedAmount);
        }
        double discountPercent = promotion.discountPercent() == null ? 0.0 : promotion.discountPercent();
        return Math.min(subtotal, subtotal * discountPercent / 100.0);
    }

    public record CartResponse(
            Long cartId,
            Long userId,
            List<CartItemDto> items,
            String promotionCode,
            AppliedPromotionDto appliedPromotion,
            Double subtotal,
            Double discountAmount,
            Double total
    ) {}
    public record CartItemDto(Long productId, Integer quantity, Double unitPrice, Double lineTotal) {}
    public record AppliedPromotionDto(
            String code,
            String discountType,
            Double discountPercent,
            Double fixedAmount,
            Double minimumOrderAmount,
            Integer usageLimit,
            Long timesUsed,
            Boolean usedByUser
    ) {}
}
