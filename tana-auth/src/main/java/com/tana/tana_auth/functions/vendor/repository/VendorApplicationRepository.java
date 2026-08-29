package com.tana.tana_auth.functions.vendor.repository;

import com.tana.tana_common.model.VendorApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorApplicationRepository extends JpaRepository<VendorApplication, Long> {
    List<VendorApplication> findByStatusOrderByApplicationIdDesc(String status);

    List<VendorApplication> findAllByOrderByApplicationIdDesc();

    Long countByStatus(String status);
}
