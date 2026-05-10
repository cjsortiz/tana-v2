package com.tana.tana_common.functions.dropdown.repository;

import com.tana.tana_common.model.DropdownMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DropdownRepository extends JpaRepository<DropdownMaster,Long> {

    @Query(value = "SELECT * FROM DropdownMaster",nativeQuery = true)
    List<DropdownMaster> fetchAllDropdown();
}
