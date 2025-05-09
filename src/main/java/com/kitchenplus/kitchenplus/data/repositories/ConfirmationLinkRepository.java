package com.kitchenplus.kitchenplus.data.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kitchenplus.kitchenplus.data.enums.LinkType;
import com.kitchenplus.kitchenplus.data.models.ConfirmationLink;

public interface ConfirmationLinkRepository extends JpaRepository<ConfirmationLink, String> {
    /**
     * Find a confirmation link by its address fragment.
     * @param addressFragment
     * @return
     */
    Optional<ConfirmationLink> findByAddressFragment(String addressFragment);

    /**
     * Delete all confirmation links for a user with the given userId and type.
     * @param userId
     * @param type
     */
    void deleteByUserIdAndType(Long userId, LinkType type);
}
