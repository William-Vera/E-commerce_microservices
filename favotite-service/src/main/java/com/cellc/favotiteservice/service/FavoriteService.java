package com.cellc.favotiteservice.service;

import com.cellc.favotiteservice.entity.Favorite;
import com.cellc.favotiteservice.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Transactional
    public void addFavorite(Long userId, Long productId) {
        favoriteRepository.findByUserIdAndProductId(userId, productId).ifPresent(f -> {
        });

        favoriteRepository.findByUserIdAndProductId(userId, productId).orElseGet(() -> {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setProductId(productId);
            return favoriteRepository.save(f);
        });
    }

    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        favoriteRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(favoriteRepository::delete);
    }

    public List<Long> listProductIds(Long userId) {
        return favoriteRepository.findByUserId(userId).stream().map(Favorite::getProductId).toList();
    }
}

