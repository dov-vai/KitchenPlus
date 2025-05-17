package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.data.models.Set;
import com.kitchenplus.kitchenplus.data.models.SetItem;
import com.kitchenplus.kitchenplus.data.repositories.SetItemRepository;
import com.kitchenplus.kitchenplus.data.repositories.SetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SetService {

    private final SetRepository setRepository;
    private final SetItemRepository setItemRepository;

    public SetService(SetRepository setRepository, SetItemRepository setItemRepository) {
        this.setRepository = setRepository;
        this.setItemRepository = setItemRepository;
    }

    public Optional<Set> get(Long id) {
        return setRepository.findById(id);
    }

    public List<Set> getAll() {
        return setRepository.findAll();
    }

    public Set save(Set set) {
        return setRepository.save(set);
    }

    public Optional<SetItem> getSetItem(Long id) {
        return setItemRepository.findById(id);
    }
}
