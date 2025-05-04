package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.data.models.Set;
import com.kitchenplus.kitchenplus.data.repositories.SetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SetService {

    private final SetRepository setRepository;

    public SetService(SetRepository setRepository) {
        this.setRepository = setRepository;
    }

    public Optional<Set> get(Long id) {
        return setRepository.findById(id);
    }

    public List<Set> getAll() {
        return setRepository.findAll();
    }

    public Set insert(Set set) {
        return setRepository.save(set);
    }

}
