package com.tana.tana_auth.functions.routes.repository;

import com.tana.tana_common.model.RouteCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouteCategoryRepository extends JpaRepository<RouteCategories, Long> {
    Optional<RouteCategories> findByCategoryNameIgnoreCase(String categoryName);
}
