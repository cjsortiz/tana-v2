package com.tana.tana_auth.functions.places.repository;

import com.tana.tana_common.model.Reflection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReflectionRepository extends JpaRepository<Reflection, Long> {

    Page<Reflection> findByPlaceIdOrderByCreatedAtDesc(
            Long placeId,
            Pageable pageable
    );
}
