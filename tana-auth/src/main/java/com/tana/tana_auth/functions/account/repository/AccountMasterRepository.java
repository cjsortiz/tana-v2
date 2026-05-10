package com.tana.tana_auth.functions.account.repository;

import com.tana.tana_auth.functions.account.dto.AccountBasicDetailsDto;
import com.tana.tana_auth.functions.account.dto.AccountResponseDto;
import com.tana.tana_common.model.AccountMaster;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountMasterRepository extends JpaRepository<AccountMaster , Long> {

    @Query(value = "SELECT isIpBlocked FROM AccountMaster " +
            "WHERE userName = :userName",nativeQuery = true)
    boolean isIpBlocked(@Param("userName") String userName);

    @Query(value = "SELECT * FROM AccountMaster " +
            "WHERE userName = :userName",nativeQuery = true)
    AccountMaster findByUserName(@Param("userName")String userName);

    @Query("""
    SELECT new com.tana.tana_auth.functions.account.dto.AccountBasicDetailsDto(
        a.id,
        CONCAT(COALESCE(a.firstName, ''), ' ', COALESCE(a.lastName, '')),
        COUNT(DISTINCT sv.visitedId),
        0L,
        0L,
        a.userType,
        a.userImage,
        a.firstName,
        a.lastName,
        a.userLocation,
        a.bio
    )
    FROM AccountMaster a
    LEFT JOIN a.visitedSpots sv ON sv.isVisited = true
    WHERE a.id = :accountId
    GROUP BY a.id, a.firstName, a.lastName
    """)
    AccountBasicDetailsDto getAccountBasic(@Param("accountId") Long accountId);
}
