package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.ComboItem;
import com.hoabanrestaurant.backend.entity.ComboItem.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ComboItemRepository extends JpaRepository<ComboItem, Id> {

    List<ComboItem> findByCombo_Id(UUID comboId);

    void deleteAllByComboId(UUID comboId);

    @Query("""
              SELECT ci FROM ComboItem ci
              WHERE ci.combo.id = :comboId AND ci.dish.id IN :dishIds
            """)
    List<ComboItem> findAnyExists(UUID comboId, List<UUID> dishIds);
}
