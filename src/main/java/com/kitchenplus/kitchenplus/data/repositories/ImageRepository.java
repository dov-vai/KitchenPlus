package com.kitchenplus.kitchenplus.data.repositories;

import com.kitchenplus.kitchenplus.data.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}