package com.kitchenplus.kitchenplus.data.services;


import com.kitchenplus.kitchenplus.data.models.Image;
import com.kitchenplus.kitchenplus.data.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
    }

    public List<Image> getImagesByItemId(Long itemId) {
        return imageRepository.findAll().stream()
                .filter(image -> image.getItem().getId().equals(itemId))
                .collect(Collectors.toList());
    }

    public void saveAllImages(List<Image> images) {
        imageRepository.saveAll(images);
    }

    public void deleteByItemId(Long itemId) {
        List<Image> images = getImagesByItemId(itemId);
        imageRepository.deleteAll(images);
    }
}