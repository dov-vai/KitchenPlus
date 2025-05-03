package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.data.models.DeliveryAddress;
import com.kitchenplus.kitchenplus.data.repositories.DeliveryAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class DeliveryAddressService {
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    public  void postAddress(DeliveryAddress deliveryAddress) {
        try {
            deliveryAddressRepository.save(deliveryAddress);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while saving address: " + e.getMessage());
        }

    }

}
