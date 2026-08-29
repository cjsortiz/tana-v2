package com.tana.tana_auth.functions.vendor.repository;

import com.tana.tana_common.model.VendorInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorInviteRepository extends JpaRepository<VendorInvite, Long> {
    Optional<VendorInvite> findByToken(String token);
}
