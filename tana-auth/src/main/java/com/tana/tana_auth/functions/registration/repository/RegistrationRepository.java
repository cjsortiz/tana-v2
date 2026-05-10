package com.tana.tana_auth.functions.registration.repository;

import com.tana.tana_common.model.AccountMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<AccountMaster, Long> {

    @Query(value = "Select * From AccountMaster " +
            "Where userName = :userName ", nativeQuery = true)
    AccountMaster fetchAccountByUserName(@Param("userName") String userName);
}
