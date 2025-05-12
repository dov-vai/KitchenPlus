package com.kitchenplus.kitchenplus.data.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.kitchenplus.kitchenplus.data.models.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @NonNull Optional<Client> findById(@NonNull Long id);
}

