package com.tana.tana_common.functions.accountmaster.repository;

import com.tana.tana_common.model.AccountMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountMaster , Long> {

    @Query(value = "SELECT * FROM AccountMaster " +
            "WHERE userName = :userName",nativeQuery = true)
    AccountMaster findByUserName(@Param("userName")String userName);
}
